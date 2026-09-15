package com.aft.api.agent;

import static org.assertj.core.api.Assertions.assertThat;

import com.aft.api.agent.tool.PendingToolRegistry;
import com.aft.api.agent.tool.ToolResult;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;

class PendingToolRegistryTest {

    private final PendingToolRegistry registry = new PendingToolRegistry();

    @Test
    void kaydedilenCagriSonucBekler() throws Exception {
        UUID callId = UUID.randomUUID();
        CompletableFuture<ToolResult> future = registry.register(callId);

        assertThat(registry.isPending(callId)).isTrue();
        registry.complete(ToolResult.ok(callId, "{\"a\":1}", false));

        assertThat(future.get(1, TimeUnit.SECONDS).contentJson()).isEqualTo("{\"a\":1}");
        assertThat(registry.isPending(callId)).isFalse();
    }

    @Test
    void bilinmeyenCagriSonucuYokSayilir() {
        assertThat(registry.complete(ToolResult.ok(UUID.randomUUID(), "{}", false))).isFalse();
    }

    @Test
    void ayniSonucIkinciKezIsleTutmaz() {
        UUID callId = UUID.randomUUID();
        registry.register(callId);

        assertThat(registry.complete(ToolResult.ok(callId, "{}", false))).isTrue();
        assertThat(registry.complete(ToolResult.ok(callId, "{}", false))).isFalse();
    }

    @Test
    void iptalEdilenCagriTamamlanmaz() {
        UUID callId = UUID.randomUUID();
        CompletableFuture<ToolResult> future = registry.register(callId);

        registry.cancel(callId);

        assertThat(future.isCancelled()).isTrue();
        assertThat(registry.isPending(callId)).isFalse();
    }

    @Test
    void yanitGelmezseZamanAsimiOlur() {
        CompletableFuture<ToolResult> future = registry.register(UUID.randomUUID());

        assertThat(org.assertj.core.api.Assertions.catchThrowable(
                () -> future.get(50, TimeUnit.MILLISECONDS))).isInstanceOf(TimeoutException.class);
    }

    @Test
    void bekleyenSayisiTakipEdilir() throws ExecutionException, InterruptedException {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        registry.register(a);
        registry.register(b);

        assertThat(registry.pendingCount()).isEqualTo(2);
        registry.complete(ToolResult.ok(a, "{}", false));
        assertThat(registry.pendingCount()).isEqualTo(1);
    }
}
