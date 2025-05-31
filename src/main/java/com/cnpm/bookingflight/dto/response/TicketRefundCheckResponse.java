package com.cnpm.bookingflight.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketRefundCheckResponse {
    TicketInfo ticketInfo;
    LocalDate earliestRefundDate;
    boolean canRefund;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class TicketInfo {
        String passengerName;
        String passengerEmail;
        String passengerPhone;
        String passengerIDCard;
        String flightCode;
        String departureAirport;
        String arrivalAirport;
        LocalDateTime departureDateTime;
        String seatName;
        int price;
    }
}