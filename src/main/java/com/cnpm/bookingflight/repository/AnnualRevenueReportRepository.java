package com.cnpm.bookingflight.repository;

import com.cnpm.bookingflight.domain.AnnualRevenueReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnualRevenueReportRepository extends JpaRepository<AnnualRevenueReport, Integer> {
}