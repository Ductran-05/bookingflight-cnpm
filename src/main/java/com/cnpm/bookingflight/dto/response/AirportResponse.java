package com.cnpm.bookingflight.dto.response;

import com.cnpm.bookingflight.domain.City;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AirportResponse {
    Long id;
    String airportCode;
    String airportName;
    Boolean canDelete;
    City city;
}