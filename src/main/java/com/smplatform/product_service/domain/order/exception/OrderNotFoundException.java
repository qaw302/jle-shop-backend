package com.smplatform.product_service.domain.order.exception;

import com.smplatform.product_service.exception.AbstractApiException;
import org.springframework.http.HttpStatus;

public class OrderNotFoundException extends AbstractApiException {

    public OrderNotFoundException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
