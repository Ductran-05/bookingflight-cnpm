package com.cnpm.bookingflight.repository;

import com.cnpm.bookingflight.domain.Flight_Seat;
import com.cnpm.bookingflight.domain.id.Flight_SeatId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Flight_SeatRepository extends JpaRepository<Flight_Seat, Flight_SeatId> {
    void deleteByIdFlightId(Long flightId);

    boolean existsBySeatId(Long seatId);

    List<Flight_Seat> findByIdFlightId(Long id);

    boolean existsByFlightIdAndSeatIdIn(Long id, List<Long> seats);
    List<Flight_Seat> findBySeatId(Long seatId);

}