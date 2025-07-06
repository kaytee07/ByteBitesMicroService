package com.bytebites.orderservice.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedOrderAccessException extends RuntimeException {
    public UnauthorizedOrderAccessException(String message) {
        super(message);
    }

    public UnauthorizedOrderAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
