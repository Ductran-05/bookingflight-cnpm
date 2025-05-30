package com.cnpm.bookingflight.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketRequest {
    @NotNull(message = "flightId must not be null")
    @Positive(message = "flightId must be positive")
    Long flightId;

    @NotEmpty(message = "tickets must not be empty")
    @Size(min = 1, message = "tickets must contain at least one entry")
    List<@Valid TicketInfo> tickets;

    @Data
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class TicketInfo {
        @NotNull(message = "seatId must not be null")
        @Positive(message = "seatId must be positive")
        Long seatId;

        @NotBlank(message = "passengerName must not be blank")
        @Size(max = 100, message = "passengerName must not exceed 100 characters")
        String passengerName;

        @NotBlank(message = "passengerPhone must not be blank")
        @Pattern(regexp = "^0\\d{9,10}$", message = "phone must be a valid phone number: starting with 0 and having 10 to 11 digits")
        String passengerPhone;

        @NotBlank(message = "passengerIDCard must not be blank")
        @Size(max = 50, message = "passengerIDCard must not exceed 50 characters")
        String passengerIDCard;

        @NotBlank(message = "passengerEmail must not be blank")
        @Email(message = "passengerEmail must be a valid email address")
        @Size(max = 100, message = "passengerEmail must not exceed 100 characters")
        String passengerEmail;
    }
}