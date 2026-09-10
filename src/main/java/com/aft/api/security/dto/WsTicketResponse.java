package com.aft.api.security.dto;

public record WsTicketResponse(String ticket, long expiresIn) {
}
