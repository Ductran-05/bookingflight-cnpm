package com.cnpm.bookingflight.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cnpm.bookingflight.domain.Flight_Seat;
import com.cnpm.bookingflight.domain.id.Flight_SeatId;

@Repository
public interface Flight_SeatRepository extends JpaRepository<Flight_Seat, Flight_SeatId> {
    List<Flight_Seat> findByIdFlightId(Long flightId);
}
