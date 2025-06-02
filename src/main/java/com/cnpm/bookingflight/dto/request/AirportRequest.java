package com.cnpm.bookingflight.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AirportRequest {
    @NotBlank(message = "airportCode must not be blank")
    String airportCode;

    @NotBlank(message = "airportName must not be blank")
    String airportName;

    @NotNull(message = "cityId must not be null")
    Long cityId;
}