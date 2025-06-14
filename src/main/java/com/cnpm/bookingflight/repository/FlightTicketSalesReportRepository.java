package com.cnpm.bookingflight.repository;

import com.cnpm.bookingflight.domain.FlightTicketSalesReport;
import com.cnpm.bookingflight.domain.id.FlightTicketSalesReportId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlightTicketSalesReportRepository extends JpaRepository<FlightTicketSalesReport, FlightTicketSalesReportId> {
}