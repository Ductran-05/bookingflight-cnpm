package com.cnpm.bookingflight.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketRequest {
    String flightId;
    String seatId;
    String passengerName;
    String passengerPhone;
    String passengerIDCard;
    String passengerEmail;
    Boolean isPaid;
}
