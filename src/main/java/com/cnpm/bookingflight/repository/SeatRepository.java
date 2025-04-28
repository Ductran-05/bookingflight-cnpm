package com.cnpm.bookingflight.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cnpm.bookingflight.domain.Seat;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    Seat findBySeatCode(String seatCode);
}
