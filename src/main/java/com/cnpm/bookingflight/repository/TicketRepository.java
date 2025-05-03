package com.cnpm.bookingflight.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cnpm.bookingflight.domain.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

}
