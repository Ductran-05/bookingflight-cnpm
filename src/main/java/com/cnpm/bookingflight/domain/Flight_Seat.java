package com.cnpm.bookingflight.domain;

import com.cnpm.bookingflight.domain.id.Flight_SeatId;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Flight_Seat {
    @EmbeddedId
    Flight_SeatId id;

    @ManyToOne
    @MapsId("flightId")
    @JoinColumn(name = "flight_id")
    Flight flight;

    @ManyToOne
    @MapsId("seatId")
    @JoinColumn(name = "seat_id")
    Seat seat;

    Integer quantity;
    Integer remainingTickets;
    Integer price;
}
