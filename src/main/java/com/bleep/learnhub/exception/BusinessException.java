package com.bleep.learnhub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) // Tells Spring to return a 400 if not caught
public class BusinessException extends RuntimeException {
    
    public BusinessException(String message) {
        super(message);
    }
}