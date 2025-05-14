package com.cnpm.bookingflight.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketRequest {
    Long flightId;
    List<TicketInfo> tickets;

    @Data
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class TicketInfo {
        Long seatId;
        String passengerName;
        String passengerPhone;
        String passengerIDCard;
        String passengerEmail;
    }
}