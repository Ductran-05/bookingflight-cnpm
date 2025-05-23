package com.cnpm.bookingflight.domain;

import java.time.LocalDateTime;

import com.cnpm.bookingflight.domain.id.Flight_AirportId;
import com.fasterxml.jackson.annotation.JsonInclude;

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
@JsonInclude(JsonInclude.Include.NON_NULL)
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

    LocalDateTime departureDateTime;
    LocalDateTime arrivalDateTime;
    String note;
}
