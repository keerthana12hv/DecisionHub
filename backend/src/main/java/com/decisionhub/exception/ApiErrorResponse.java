package com.decisionhub.exception;

import java.time.LocalDateTime;

public class ApiErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String path;

    public ApiErrorResponse(int status, String error, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.path = path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getPath() {
        return path;
    }
}