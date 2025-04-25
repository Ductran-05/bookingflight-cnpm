package com.cnpm.bookingflight.domain;

import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Parameters {
    Integer maxInterQuantity;
    Integer minInterQuantity;
    Integer minFlightTime;
    Integer minStopTime;
    Integer maxStopTime;
    Integer latestBookingDay;
    Integer latestCancelDay;
}
