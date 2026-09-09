package com.aft.api.common.exception;

public class ValidationException extends ApiException {

    public ValidationException(String detail) {
        super(ErrorCode.VALIDATION_FAILED, detail);
    }
}
