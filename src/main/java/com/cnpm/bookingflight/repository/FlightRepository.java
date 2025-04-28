package com.cnpm.bookingflight.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cnpm.bookingflight.domain.Flight;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
    Flight findByFlightCode(String flightCode);
}
