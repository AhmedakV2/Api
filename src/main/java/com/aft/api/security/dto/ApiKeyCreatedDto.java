package com.aft.api.security.dto;

public record ApiKeyCreatedDto(ApiKeyDto key, String secret) {
}
