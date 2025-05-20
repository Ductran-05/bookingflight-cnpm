package com.cnpm.bookingflight.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
public class DashboardReport {
    @Id
    Long id;

    int year;
    double revenueThisYear;
    double revenueLastYear;
    int flightCountThisYear;
    int flightCountLastYear;
    int airlineCount;
    int airportCount;
    String topAirlines;
}