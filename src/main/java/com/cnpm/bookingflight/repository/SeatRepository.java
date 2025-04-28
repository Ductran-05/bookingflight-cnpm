package com.cnpm.bookingflight.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cnpm.bookingflight.domain.Seat;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    Seat findBySeatCode(String seatCode);
}
