package com.aft.api.common.exception;

public class NotFoundException extends ApiException {
    public NotFoundException(String detail) {

        super(ErrorCode.NOT_FOUND, detail);
    }
    public static NotFoundException of(String entity, Object id) {
        return new NotFoundException(entity + " " + id + " bulunamadi");
    }
}
