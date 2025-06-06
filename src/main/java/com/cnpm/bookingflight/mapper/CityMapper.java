package com.cnpm.bookingflight.mapper;

import com.cnpm.bookingflight.domain.City;
import com.cnpm.bookingflight.dto.request.CityRequest;
import com.cnpm.bookingflight.dto.response.CityResponse;
import com.cnpm.bookingflight.repository.AirportRepository;
import org.springframework.stereotype.Component;

@Component
public class CityMapper {

    private final AirportRepository airportRepository;

    public CityMapper(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    public City toCity(CityRequest request) {
        return City.builder()
                .cityCode(request.getCityCode())
                .cityName(request.getCityName())
                .build();
    }

    public CityResponse toCityResponse(City city) {
        boolean hasForeignKey = airportRepository.existsByCityId(city.getId());
        return CityResponse.builder()
                .id(city.getId())
                .cityCode(city.getCityCode())
                .cityName(city.getCityName())
                .canDelete(!hasForeignKey)
                .build();
    }
}