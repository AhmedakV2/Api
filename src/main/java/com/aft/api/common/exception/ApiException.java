package com.aft.api.common.exception;

public class ApiException extends RuntimeException {
    private final ErrorCode code;

    public ApiException(ErrorCode code, String detail) {
        super(detail);
        this.code = code;
    }
    public ErrorCode code() {
        return code;
    }
}
