package com.aft.api.agent.service;

import com.aft.api.agent.dto.AgentResponse;
import com.aft.api.agent.entity.AgentMessage;
import com.aft.api.agent.entity.AgentSession;
import com.aft.api.agent.entity.MessageRole;
import com.aft.api.agent.memory.ConversationWindow;
import com.aft.api.agent.prompt.PromptLibrary;
import com.aft.api.agent.prompt.SystemPrompts;
import com.aft.api.agent.routing.IntentRouter;
import com.aft.api.agent.routing.ToolIntent;
import com.aft.api.agent.provider.OllmProvider;
import com.aft.api.agent.provider.ModelRouter;
import com.aft.api.agent.tool.ToolCallContext;
import com.aft.api.agent.tool.ToolRegistry;
import com.aft.api.agent.tool.ToolSpec;
import com.aft.api.common.exception.ApiException;
import com.aft.api.common.exception.ErrorCode;
import com.aft.api.config.AiProperties;
import com.aft.api.agent.dto.ChatFrame;
import com.aft.api.realtime.ChatChannel;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.ToolCallback;
import java.util.Map;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;

@Service
public class AgentBrainService {
    private static final Logger log = LoggerFactory.getLogger(AgentBrainService.class);

    private final AgentSessionManager sessionManager;
    private final ConversationWindow conversationWindow;
    private final PromptLibrary promptLibrary;
    private final ModelRouter modelRouter;
    private final ModelUsageService usageService;
    private final ChatChannel chat;
    private final ToolRegistry toolRegistry;
    private final IntentRouter intentRouter;
    private final Executor toolExecutor;
    private final AiProperties properties;

    public AgentBrainService(AgentSessionManager sessionManager,
                             ConversationWindow conversationWindow,
                             PromptLibrary promptLibrary,
                             ModelRouter modelRouter,
                             ModelUsageService usageService,
                             ChatChannel chat,
                             ToolRegistry toolRegistry,
                             IntentRouter intentRouter,
                             @Qualifier("agentToolExecutor") Executor toolExecutor,
                             AiProperties properties) {
        this.sessionManager = sessionManager;
        this.conversationWindow = conversationWindow;
        this.promptLibrary = promptLibrary;
        this.modelRouter = modelRouter;
        this.usageService = usageService;
        this.chat = chat;
        this.toolRegistry = toolRegistry;
        this.intentRouter = intentRouter;
        this.toolExecutor = toolExecutor;
        this.properties = properties;
    }

    public AgentResponse respond(UUID sessionId, UUID userId, String content, String model) {
        AgentSession session = sessionManager.retune(sessionId, userId, model);
        sessionManager.append(sessionId, MessageRole.USER, content, ConversationWindow.estimate(content));

        ToolIntent intent = routeIntent(session, content);
        List<Message> prompt = buildPrompt(session, intent);
        AgentMessage placeholder = sessionManager.append(sessionId, MessageRole.ASSISTANT, "", 0);
        ToolCallContext toolContext = new ToolCallContext(sessionId, userId, session.getDeviceId());
        toolContext.bindMessage(placeholder.getId());

        ChatResponse response;
        try {
            response = callModel(session, prompt, toolContext, intent);
        } catch (RuntimeException e) {
            sessionManager.discard(placeholder.getId());
            throw e;
        }

        String text = textOf(response);
        AgentMessage saved = sessionManager.revise(placeholder.getId(), text,
                ConversationWindow.estimate(text));
        int tokenIn = tokenIn(response);
        int tokenOut = tokenOut(response);
        usageService.recordCounts(session.getOrgId(), userId, sessionId, session.getModel(), tokenIn, tokenOut);

        return new AgentResponse(sessionId, saved.getId(), saved.getSeq(), text,
                session.getModel(), tokenIn, tokenOut);
    }

    public void streamInto(UUID sessionId, UUID userId, String content, String model, String turnId) {
        AgentSession session = sessionManager.retune(sessionId, userId, model);
        UUID deviceId = session.getDeviceId();
        if (!chat.isReachable(deviceId)) {
            throw new ApiException(ErrorCode.VALIDATION_FAILED,
                    "Bu oturum icin bagli bir istemci kanali yok");
        }
        sessionManager.append(sessionId, MessageRole.USER, content, ConversationWindow.estimate(content));

        ToolIntent intent = routeIntent(session, content);
        toolExecutor.execute(() -> runTurn(sessionId, userId, session, intent, turnId));
    }

    private void runTurn(UUID sessionId, UUID userId, AgentSession session,
                         ToolIntent intent, String turnId) {
        if (intentRouter.offersTools(intent)) {
            blockingInto(sessionId, userId, session, turnId, intent);
            return;
        }
        streamingInto(sessionId, userId, session, turnId, intent);
    }

    private void streamingInto(UUID sessionId, UUID userId, AgentSession session,
                               String turnId, ToolIntent intent) {
        UUID deviceId = session.getDeviceId();
        List<Message> prompt = buildPrompt(session, intent);
        AgentMessage placeholder = sessionManager.append(sessionId, MessageRole.ASSISTANT, "", 0);
        ToolCallContext toolContext = new ToolCallContext(sessionId, userId, session.getDeviceId());
        toolContext.bindMessage(placeholder.getId());

        StringBuilder buffer = new StringBuilder();
        AtomicInteger tokenIn = new AtomicInteger();
        AtomicInteger tokenOut = new AtomicInteger();
        AtomicBoolean stopped = new AtomicBoolean();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch finished = new CountDownLatch(1);

        Disposable subscription = modelRouter.provider()
                .stream(prompt, options(session, toolContext, intent))
                .doOnNext(chunk -> {
                    String piece = textOf(chunk);
                    if (!piece.isEmpty()) {
                        buffer.append(piece);
                        chat.send(deviceId, ChatFrame.delta(turnId, sessionId, piece));
                    }
                    captureUsage(chunk, tokenIn, tokenOut);
                })
                .doOnError(error -> failure.set(error))
                .doFinally(signal -> finished.countDown())
                .subscribe();

        chat.begin(sessionId, turnId, () -> {
            stopped.set(true);
            subscription.dispose();
        });

        try {
            awaitTurn(finished);
            if (failure.get() != null) {
                throw new IllegalStateException(failure.get());
            }
            String text = buffer.toString();
            AgentMessage saved = sessionManager.revise(placeholder.getId(), text,
                    ConversationWindow.estimate(text));
            usageService.recordCounts(session.getOrgId(), userId, sessionId, session.getModel(),
                    tokenIn.get(), tokenOut.get());
            chat.send(deviceId, ChatFrame.done(turnId, sessionId, saved.getId(), session.getModel()));
        } catch (RuntimeException e) {
            String text = buffer.toString();
            if (text.isBlank()) {
                sessionManager.discard(placeholder.getId());
            } else {
                sessionManager.revise(placeholder.getId(), text, ConversationWindow.estimate(text));
            }
            if (stopped.get()) {
                chat.send(deviceId, ChatFrame.error(turnId, sessionId, "Uretim durduruldu"));
            } else {
                log.error("Model akisi basarisiz sessionId={}", sessionId, e);
                chat.send(deviceId, ChatFrame.error(turnId, sessionId,
                        "Model saglayici yanit vermedi: " + rootMessage(e)));
            }
        } finally {
            chat.finish(sessionId, turnId);
        }
    }

    private void blockingInto(UUID sessionId, UUID userId, AgentSession session,
                              String turnId, ToolIntent intent) {
        UUID deviceId = session.getDeviceId();
        List<Message> prompt = buildPrompt(session, intent);
        AgentMessage placeholder = sessionManager.append(sessionId, MessageRole.ASSISTANT, "", 0);
        ToolCallContext toolContext = new ToolCallContext(sessionId, userId, session.getDeviceId());
        toolContext.bindMessage(placeholder.getId());
        Thread worker = Thread.currentThread();
        chat.begin(sessionId, turnId, worker::interrupt);

        try {
            ChatResponse response = modelRouter.provider()
                    .call(prompt, options(session, toolContext, intent));
            String text = textOf(response);
            AgentMessage saved = sessionManager.revise(placeholder.getId(), text,
                    ConversationWindow.estimate(text));
            usageService.recordCounts(session.getOrgId(), userId, sessionId, session.getModel(),
                    tokenIn(response), tokenOut(response));
            if (!text.isEmpty()) {
                chat.send(deviceId, ChatFrame.delta(turnId, sessionId, text));
            }
            chat.send(deviceId, ChatFrame.done(turnId, sessionId, saved.getId(), session.getModel()));
        } catch (RuntimeException e) {
            log.error("Aracli tur basarisiz sessionId={}", sessionId, e);
            sessionManager.discard(placeholder.getId());
            chat.send(deviceId, ChatFrame.error(turnId, sessionId,
                    "Model saglayici yanit vermedi: " + rootMessage(e)));
        } finally {
            chat.finish(sessionId, turnId);
        }
    }

    private void awaitTurn(CountDownLatch finished) {
        try {
            if (!finished.await(properties.requestTimeout().toMillis(), TimeUnit.MILLISECONDS)) {
                throw new IllegalStateException("Model yanit suresi asildi");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Tur kesildi", e);
        }
    }

    private ToolIntent routeIntent(AgentSession session, String content) {
        boolean hasHistory = !sessionManager.recentHistory(session.getId(), 2).isEmpty();
        ToolIntent intent = intentRouter.route(content, hasHistory);
        log.info("Niyet cozumlendi sessionId={} niyet={}", session.getId(), intent);
        return intent;
    }

    private List<Message> buildPrompt(AgentSession session, ToolIntent intent) {
        List<ToolSpec> tools = intentRouter.offersTools(intent)
                ? toolRegistry.catalogFor(session.getDeviceId(), intentRouter.toolsFor(intent))
                : List.of();
        String system = promptLibrary.system(SystemPrompts.PLANNER, session.getMode(), tools);
        return conversationWindow.build(system,
                sessionManager.recentHistory(session.getId(), properties.maxWindowMessages()));
    }

    private ChatResponse callModel(AgentSession session, List<Message> prompt,
                                   ToolCallContext toolContext, ToolIntent intent) {
        try {
            return modelRouter.provider().call(prompt, options(session, toolContext, intent));
        } catch (RuntimeException e) {
            log.error("Model cagrisi basarisiz sessionId={}", session.getId(), e);
            throw new ApiException(ErrorCode.AI_PROVIDER_ERROR, "Model saglayici yanit vermedi");
        }
    }

    private void captureUsage(ChatResponse response, AtomicInteger tokenIn, AtomicInteger tokenOut) {
        int in = tokenIn(response);
        int out = tokenOut(response);
        if (in > 0) {
            tokenIn.set(in);
        }
        if (out > 0) {
            tokenOut.set(out);
        }
    }

    private String rootMessage(Throwable error) {
        Throwable cursor = error;
        while (cursor.getCause() != null && cursor.getCause() != cursor) {
            cursor = cursor.getCause();
        }
        String message = cursor.getMessage();
        return (message == null || message.isBlank()) ? cursor.getClass().getSimpleName() : message;
    }

    private String textOf(ChatResponse response) {
        if (response == null) {
            return "";
        }
        Generation generation = response.getResult();
        if (generation == null || generation.getOutput() == null) {
            return "";
        }
        String text = generation.getOutput().getText();
        return text == null ? "" : text;
    }

    private int tokenIn(ChatResponse response) {
        if (response == null || response.getMetadata() == null || response.getMetadata().getUsage() == null) {
            return 0;
        }
        Integer value = response.getMetadata().getUsage().getPromptTokens();
        return value == null ? 0 : value;
    }

    private int tokenOut(ChatResponse response) {
        if (response == null || response.getMetadata() == null || response.getMetadata().getUsage() == null) {
            return 0;
        }
        Integer value = response.getMetadata().getUsage().getCompletionTokens();
        return value == null ? 0 : value;
    }

    private ToolCallingChatOptions options(AgentSession session, ToolCallContext toolContext,
                                           ToolIntent intent) {
        List<ToolCallback> callbacks = intentRouter.offersTools(intent)
                ? toolRegistry.callbacksFor(session.getDeviceId(), intentRouter.toolsFor(intent))
                : List.of();
        if (callbacks.isEmpty()) {
            log.info("Modele arac sunulmuyor sessionId={} niyet={}, dogrudan metinsel yanit uretilecek",
                    session.getId(), intent);
        } else {
            log.info("Modele {} arac sunuluyor sessionId={} niyet={}",
                    callbacks.size(), session.getId(), intent);
        }
        return modelRouter.provider().toolOptions(session.getModel(), callbacks,
                Map.of(ToolCallContext.KEY, toolContext));
    }

    public long streamTimeoutMillis() {
        return properties.requestTimeout().toMillis();
    }
}
