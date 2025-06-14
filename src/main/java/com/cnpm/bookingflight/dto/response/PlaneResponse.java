package com.cnpm.bookingflight.dto.response;

import com.cnpm.bookingflight.domain.Airline;

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
public class PlaneResponse {
    Long id;
    String planeCode;
    String planeName;
    Boolean canDelete;
    Airline airline;
}