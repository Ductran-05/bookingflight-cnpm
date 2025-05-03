package com.cnpm.bookingflight.exception;

public enum ErrorCode {
    UNIDENTIFIED_EXCEPTION(999, "Unidentified Exception"),
    NOT_FOUND(1001, "Not found"),
    EXISTED(1002, "Existed"),
    INVALID(1003, "Invalid"),
    OUT_OF_TICKETS(1004, "Out of tickets");

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private int code;
    private String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
