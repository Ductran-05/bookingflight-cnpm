package com.cnpm.bookingflight.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cnpm.bookingflight.domain.Flight_Airport;
import com.cnpm.bookingflight.domain.id.Flight_AirportId;

@Repository
public interface Flight_AirportRepository extends JpaRepository<Flight_Airport, Flight_AirportId> {
    List<Flight_Airport> findByIdFlightId(Long flightId);

    void deleteByIdFlightId(Long id);

    boolean existsByAirportId(Long id);
}