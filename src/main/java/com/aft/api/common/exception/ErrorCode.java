package com.aft.api.common.exception;


import org.springframework.http.HttpStatus;

public enum ErrorCode {
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Istek govdesi dogrulanamadi"),
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "Jeton yok veya gecersiz"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Rol veya organizasyon kapsami yetersiz"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Kayit bulunamadi"),
    CONFLICT(HttpStatus.CONFLICT, "Tekrarli e-posta veya cakisan kayit"),
    ACCOUNT_LOCKED(HttpStatus.LOCKED, "Hesap gecici olarak kilitli"),
    TOOL_FAILED(HttpStatus.FAILED_DEPENDENCY, "Istemci arac cagrisi basarisiz oldu"),
    RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "Hiz siniri veya token butcesi asildi"),
    AI_PROVIDER_ERROR(HttpStatus.BAD_GATEWAY, "Model saglayici yanit vermedi"),
    TOOL_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "Istemci arac cagrisi suresinde yanitlamadi");

    private final HttpStatus status;
    private final String title;

    ErrorCode(HttpStatus status, String title) {
        this.status = status;
        this.title = title;
    }

    public HttpStatus status() {
        return status;
    }

    public String title() {
        return title;
    }

}
