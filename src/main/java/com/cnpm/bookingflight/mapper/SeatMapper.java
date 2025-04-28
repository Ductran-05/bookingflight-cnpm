package com.cnpm.bookingflight.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cnpm.bookingflight.domain.Seat;
import com.cnpm.bookingflight.dto.request.SeatRequest;

@Mapper(componentModel = "spring")
public interface SeatMapper {
    @Mapping(target = "id", ignore = true)
    public Seat toSeat(SeatRequest request);
}
