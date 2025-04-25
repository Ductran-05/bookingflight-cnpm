package com.cnpm.bookingflight.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cnpm.bookingflight.domain.Airport;

@Repository
public interface AirportRepository extends JpaRepository<Airport, Long> {

    Airport findByAirportCode(String airportCode);

}
