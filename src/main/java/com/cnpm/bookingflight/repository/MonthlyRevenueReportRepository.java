package com.cnpm.bookingflight.repository;

import com.cnpm.bookingflight.domain.MonthlyRevenueReport;
import com.cnpm.bookingflight.domain.id.MonthlyRevenueReportId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonthlyRevenueReportRepository extends JpaRepository<MonthlyRevenueReport, MonthlyRevenueReportId> {
}