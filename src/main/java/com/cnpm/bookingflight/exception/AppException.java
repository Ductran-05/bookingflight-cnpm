package com.cnpm.bookingflight.exception;

public class AppException extends RuntimeException {
    private ErrorCode errorCode;
    private String customMessage;

    public AppException(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.customMessage = errorCode.getMessage();
    }

    public AppException(ErrorCode errorCode, String customMessage) {
        this.errorCode = errorCode;
        this.customMessage = customMessage;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getCustomMessage() {
        return customMessage;
    }
}