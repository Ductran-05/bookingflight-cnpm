package com.cnpm.bookingflight.dto.request;

import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParametersRequest {
    @Min(value = 1, message = "maxInterQuantity must be greater than 0")
    Integer maxInterQuantity;

    @Min(value = 1, message = "minInterQuantity must be greater than 0")
    Integer minInterQuantity;

    @Min(value = 1, message = "minFlightTime must be greater than 0")
    Integer minFlightTime;

    @Min(value = 1, message = "minStopTime must be greater than 0")
    Integer minStopTime;

    @Min(value = 1, message = "maxStopTime must be greater than 0")
    Integer maxStopTime;

    @Min(value = 1, message = "latestBookingDay must be greater than 0")
    Integer latestBookingDay;

    @Min(value = 1, message = "latestCancelDay must be greater than 0")
    Integer latestCancelDay;
}