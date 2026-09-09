package com.aft.api.common.exception;

public class ConflictException extends ApiException {

    public ConflictException(String detail) {
        super(ErrorCode.CONFLICT, detail);
    }
}
