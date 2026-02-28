package com.viking.ai.novel.infrastructure.excep;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
