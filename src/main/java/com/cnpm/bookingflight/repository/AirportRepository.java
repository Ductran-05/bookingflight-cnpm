package com.cnpm.bookingflight.repository;

import com.cnpm.bookingflight.domain.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AirportRepository extends JpaRepository<Airport, Long>, JpaSpecificationExecutor<Airport> {
    Airport findByAirportCode(String airportCode);
    boolean existsByCityId(Long cityId);
    List<Airport> findByCityId(Long cityId);
}