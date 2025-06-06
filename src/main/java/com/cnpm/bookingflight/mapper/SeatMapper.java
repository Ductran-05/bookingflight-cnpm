package com.cnpm.bookingflight.mapper;

import com.cnpm.bookingflight.domain.Seat;
import com.cnpm.bookingflight.dto.request.SeatRequest;
import com.cnpm.bookingflight.dto.response.SeatResponse;
import com.cnpm.bookingflight.repository.Flight_SeatRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class SeatMapper {

    @Autowired
    protected Flight_SeatRepository flightSeatRepository;

    @Mapping(target = "id", ignore = true)
    public abstract Seat toSeat(SeatRequest request);

    public SeatResponse toSeatResponse(Seat seat) {
        boolean hasForeignKey = flightSeatRepository.existsBySeatId(seat.getId());
        return SeatResponse.builder()
                .id(seat.getId())
                .seatCode(seat.getSeatCode())
                .seatName(seat.getSeatName())
                .canDelete(!hasForeignKey)
                .build();
    }
}