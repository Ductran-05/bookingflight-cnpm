package com.cnpm.bookingflight.repository;

import com.cnpm.bookingflight.domain.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long>, JpaSpecificationExecutor<Flight> {
    Flight findByFlightCode(String flightCode);

    @Query("SELECT COALESCE(COUNT(f), 0) " +
            "FROM Flight f " +
            "WHERE f.isDeleted = false " +
            "AND YEAR(f.departureDate) = :year AND MONTH(f.departureDate) = :month")
    long countFlightsByMonth(int year, int month);

    @Query("SELECT COALESCE(COUNT(f), 0) " +
            "FROM Flight f " +
            "WHERE f.isDeleted = false " +
            "AND YEAR(f.departureDate) = :year")
    long countFlightsByYear(int year);

    boolean existsByDepartureAirportIdOrArrivalAirportId(Long departureAirportId, Long arrivalAirportId);

    boolean existsByPlaneId(Long planeId);
}