package com.cnpm.bookingflight.mapper;

import com.cnpm.bookingflight.domain.City;
import com.cnpm.bookingflight.dto.request.CityRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CityMapper {
    @Mapping(target = "airports", ignore = true)
    @Mapping(target = "id", ignore = true)
    City toCity(CityRequest cityRequest);
}
