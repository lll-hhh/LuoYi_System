package com.lorries.mobile.exception;

/**
 * 禁止访问异常
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException() {
        super("禁止访问");
    }

    public ForbiddenException(String message) {
        super(message);
    }
}
