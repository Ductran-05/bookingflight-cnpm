package com.cnpm.bookingflight.exception;

public enum ErrorCode {
    UNIDENTIFIED_EXCEPTION(999, "Unidentified Exception"),
    NOT_FOUND(1001, "Not found"),
    EXISTED(1002, "Existed"),
    INVALID(1003, "Invalid"),
    OUT_OF_TICKETS(1004, "Out of tickets"),
    NO_FLIGHTS_FOUND(1005, "No flights found in this time"),
    INVALID_FILE(1006, "Invalid file"),
    UNSUPPORTED_FILE_TYPE(1007, "Unsupported File Type"),
    INVALID_INPUT(1003, "Username or password is invalid"),
    ROLE_NOT_FOUND(1008, "Role not found"),
    FLIGHT_HAS_TICKETS(1009, "Flight has booked tickets and cannot be updated or deleted" ),
    INVALID_REPORT_DATE(1010, "Reports can only be retrieved for months before the current month" ),
    REPORT_NOT_FOUND(1011, "Report not found" ),
    INVALID_PASSWORD(1012,  "Invalid password" ),;

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