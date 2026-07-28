package com.decisionhub.exception;

public class DecisionLockedException extends RuntimeException {
    public DecisionLockedException(String message) {
        super(message);
    }
}
