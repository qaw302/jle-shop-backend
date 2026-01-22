package com.smplatform.product_service.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends AbstractApiException{

    public BadRequestException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
