package com.aft.api.common.exception;

public class ForbiddenException extends ApiException {

    public ForbiddenException(String detail) {
        super(ErrorCode.FORBIDDEN, detail);
    }
}
