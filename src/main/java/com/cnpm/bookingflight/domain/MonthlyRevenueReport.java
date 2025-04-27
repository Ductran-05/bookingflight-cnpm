package com.cnpm.bookingflight.domain;

import com.cnpm.bookingflight.domain.id.MonthlyRevenueReportId;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class MonthlyRevenueReport {

    @EmbeddedId
    MonthlyRevenueReportId id;

    double revenue;
    double percentage;
    int flightCount;
}
