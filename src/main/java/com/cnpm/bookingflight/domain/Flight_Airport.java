package com.cnpm.bookingflight.domain;

import java.time.LocalDate;
import java.time.LocalTime;

import com.cnpm.bookingflight.domain.id.Flight_AirportId;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Flight_Airport {
    @EmbeddedId
    Flight_AirportId id;

    @ManyToOne
    @MapsId("flightId")
    @JoinColumn(name = "flight_id")
    Flight flight;

    @ManyToOne
    @MapsId("airportId")
    @JoinColumn(name = "airport_id")
    Airport airport;

    LocalDate departureDate;
    LocalDate arrivalDate;
    LocalTime departureTime;
    LocalTime arrivalTime;
    String note;
}
