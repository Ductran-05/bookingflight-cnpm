package com.cnpm.bookingflight.repository;

import com.cnpm.bookingflight.domain.Airline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AirlineRepository extends JpaRepository<Airline, Long>, JpaSpecificationExecutor<Airline> {

    Airline findByAirlineCode(String airlineCode);

    @Query("SELECT a.id, a.airlineName, COALESCE(COUNT(t.id), 0) " +
            "FROM Airline a " +
            "LEFT JOIN Plane p ON p.airline.id = a.id " +
            "LEFT JOIN Flight f ON f.plane.id = p.id " +
            "LEFT JOIN Ticket t ON t.flight.id = f.id " +
            "WHERE a.isDeleted = false " +
            "GROUP BY a.id, a.airlineName")
    List<Object[]> countTicketsByAirline();
}