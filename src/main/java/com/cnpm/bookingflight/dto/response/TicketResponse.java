package com.cnpm.bookingflight.dto.response;

import com.cnpm.bookingflight.domain.Account;
import com.cnpm.bookingflight.domain.Seat;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketResponse {
    Long id;
    FlightTicketResponse flight;
    String ticketCode;
    Seat seat;
    String passengerName;
    String passengerEmail;
    String passengerPhone;
    String passengerIDCard;
    Account userBooking;
}