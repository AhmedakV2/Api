package com.aft.api.security.service;

import com.aft.api.config.SecurityProperties;
import com.aft.api.common.exception.ApiException;
import com.aft.api.common.exception.ErrorCode;
import com.aft.api.security.TokenHashing;
import com.aft.api.state.KeyValueStore;
import com.aft.api.state.StateNamespaces;
import com.aft.api.state.StateStoreProvider;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class WsTicketService {
    private final KeyValueStore tickets;
    private final SecurityProperties properties;

    public WsTicketService(StateStoreProvider stateStores, SecurityProperties properties) {
        this.tickets = stateStores.keyValue(StateNamespaces.WS_TICKET);
        this.properties = properties;
    }

    public Ticket issue(UUID userId) {
        String raw = TokenHashing.randomSecret();
        tickets.put(TokenHashing.sha256Hex(raw), userId.toString(), properties.wsTicketTtl());
        return new Ticket(raw, properties.wsTicketTtl().toSeconds());
    }

    public UUID consume(String rawTicket) {
        return tickets.take(TokenHashing.sha256Hex(rawTicket))
                .map(UUID::fromString)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHENTICATED,
                        "WebSocket bileti gecersiz veya kullanilmis"));
    }

    public record Ticket(String value, long expiresInSeconds) {
    }
}
