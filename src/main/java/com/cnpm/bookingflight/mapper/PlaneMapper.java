package com.cnpm.bookingflight.mapper;

import com.cnpm.bookingflight.domain.Plane;
import com.cnpm.bookingflight.dto.request.PlaneRequest;
import com.cnpm.bookingflight.dto.response.PlaneResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.repository.AirlineRepository;
import com.cnpm.bookingflight.repository.FlightRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class PlaneMapper {

    final AirlineRepository airlineRepository;
    final FlightRepository flightRepository;

    public Plane toPlane(PlaneRequest request) {
        return Plane.builder()
                .planeCode(request.getPlaneCode())
                .planeName(request.getPlaneName())
                .airline(airlineRepository.findById(request.getAirlineId())
                        .orElseThrow(() -> new AppException(ErrorCode.INVALID)))
                .build();
    }

    public PlaneResponse toPlaneResponse(Plane plane) {
        boolean hasForeignKey = flightRepository.existsByPlaneId(plane.getId());
        return PlaneResponse.builder()
                .id(plane.getId())
                .planeCode(plane.getPlaneCode())
                .planeName(plane.getPlaneName())
                .airline(plane.getAirline())
                .canDelete(!hasForeignKey)
                .build();
    }
}