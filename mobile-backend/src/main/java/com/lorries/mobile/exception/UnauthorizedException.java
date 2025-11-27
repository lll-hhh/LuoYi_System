package com.lorries.mobile.exception;

/**
 * 未授权异常
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("未授权访问");
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
