package com.cnpm.bookingflight.mapper;

import com.cnpm.bookingflight.domain.City;
import com.cnpm.bookingflight.dto.request.CityRequest;
import org.springframework.stereotype.Component;

@Component
public class CityMapper {
    public City toCity(CityRequest request) {
        return City.builder()
                .cityCode(request.getCityCode())
                .cityName(request.getCityName())
                .build();
    }
}
