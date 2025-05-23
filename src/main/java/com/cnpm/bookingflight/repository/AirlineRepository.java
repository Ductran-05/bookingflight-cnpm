package com.cnpm.bookingflight.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cnpm.bookingflight.domain.Airline;

import java.util.List;

@Repository
public interface AirlineRepository extends JpaRepository<Airline, Long> {

    Airline findByAirlineCode(String airlineCode);

    List<Airline> findAllByIsDeletedFalse();
}