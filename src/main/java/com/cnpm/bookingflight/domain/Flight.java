package com.cnpm.bookingflight.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Flight")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String flightCode;

    @ManyToOne
    @JoinColumn(name = "planeId")
    Plane plane;

    @ManyToOne
    @JoinColumn(name = "departureAirportId")
    Airport departureAirport;

    @ManyToOne
    @JoinColumn(name = "arrivalAirportId")
    Airport arrivalAirport;

    LocalDate departureDate;
    LocalDate arrivalDate;
    LocalTime departureTime;
    LocalTime arrivalTime;
    Integer originalPrice;

    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL)
    List<Flight_Seat> seats;

    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL)
    List<Flight_Airport> interAirports;

}
