package com.cnpm.bookingflight.repository;

import com.cnpm.bookingflight.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {
    boolean existsByFlightId(Long flightId);

    @Query("SELECT COALESCE(SUM(fs.price), 0.0) " +
            "FROM Ticket t " +
            "JOIN t.flight f " +
            "JOIN t.seat s " +
            "JOIN Flight_Seat fs ON fs.flight.id = f.id AND fs.seat.id = s.id " +
            "WHERE YEAR(f.departureDate) = :year")
    double calculateRevenueByYear(int year);

    @Query("SELECT COALESCE(SUM(fs.price), 0.0) " +
            "FROM Ticket t " +
            "JOIN t.flight f " +
            "JOIN t.seat s " +
            "JOIN Flight_Seat fs ON fs.flight.id = f.id AND fs.seat.id = s.id " +
            "WHERE YEAR(f.departureDate) = :year AND MONTH(f.departureDate) = :month")
    double calculateRevenueByMonth(int year, int month);

    @Query("SELECT COALESCE(COUNT(t), 0) " +
            "FROM Ticket t " +
            "JOIN t.flight f " +
            "WHERE YEAR(f.departureDate) = :year AND MONTH(f.departureDate) = :month")
    long countTicketsByMonth(int year, int month);

    @Query("SELECT COALESCE(SUM(fs.quantity), 0) " +
            "FROM Flight_Seat fs " +
            "JOIN fs.flight f " +
            "WHERE YEAR(f.departureDate) = :year AND MONTH(f.departureDate) = :month")
    long sumFlightSeatQuantityByMonth(int year, int month);
}