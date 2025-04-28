package com.cnpm.bookingflight.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cnpm.bookingflight.domain.Airline;

public interface AirlineRepository extends JpaRepository<Airline, Long> {

    Airline findByAirlineCode(String airlineCode);

}
