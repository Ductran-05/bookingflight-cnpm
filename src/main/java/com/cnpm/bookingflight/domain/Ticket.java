package com.cnpm.bookingflight.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@Entity
@Table(name = "Ticket")
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String ticketCode;

    @ManyToOne
    @JoinColumn(name = "flightId")
    Flight flight;

    @ManyToOne
    @JoinColumn(name = "seatId")
    Seat seat;

    String passengerName;
    String passengerEmail;
    String passengerPhone;
    String passengerIDCard;
    Boolean isPaid;

    @ManyToOne
    @JoinColumn(name = "userBookingId")
    Account userBooking;
}