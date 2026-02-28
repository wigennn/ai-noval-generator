package com.viking.ai.novel.infrastructure.excep;

public class NotLoggedInException extends RuntimeException {
    public NotLoggedInException(String message) {
        super(message);
    }
}
