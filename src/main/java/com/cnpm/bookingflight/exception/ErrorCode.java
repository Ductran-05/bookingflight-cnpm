package com.cnpm.bookingflight.exception;

public enum ErrorCode {
    UNAUTHORIZED(401, "Unauthorized"),
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
    FLIGHT_HAS_TICKETS(1009, "Flight has booked tickets and cannot be updated or deleted"),
    INVALID_REPORT_DATE(1010, "Reports can only be retrieved for months before the current month"),
    REPORT_NOT_FOUND(1011, "Report not found"),
    INVALID_PASSWORD(1012, "Invalid password"),
    INVALID_FLIGHT_DURATION(1013, "Flight duration must be at least the minimum required minutes"),
    INVALID_INTER_AIRPORTS(1014, "Number of intermediate airports exceeds the maximum allowed"),
    INVALID_STOP_DURATION(1015, "Stop duration must be between the minimum and maximum allowed minutes"),
    INVALID_TICKET_INFO(1016, "Invalid ticket information"),
    INVALID_BOOKING_TIME(1017, "Booking time is too late"),
    ACCOUNT_INACTIVE(1018, "Account is inactive"),
    INVALID_PERIOD_TYPE(1019, "Invalid period type, must be 'month' or 'year'"),
    CANNOT_REFUND(1020, "Cannot refund ticket"),
    FORBIDDEN(403, "Forbidden"), INVALID_AIRPORT(1021, "" );

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