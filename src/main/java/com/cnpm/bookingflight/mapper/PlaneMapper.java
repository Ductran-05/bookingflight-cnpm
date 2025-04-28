package com.cnpm.bookingflight.mapper;

import org.springframework.stereotype.Component;

import com.cnpm.bookingflight.domain.Plane;
import com.cnpm.bookingflight.dto.request.PlaneRequest;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.repository.AirlineRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class PlaneMapper {

    final AirlineRepository airlineRepository;

    public Plane toPlane(PlaneRequest request) {
        return Plane.builder()
                .planeCode(request.getPlaneCode())
                .planeName(request.getPlaneName())
                .airline(airlineRepository.findById(request.getAirlineId())
                        .orElseThrow(() -> new AppException(ErrorCode.INVALID)))
                .build();
    }
}
