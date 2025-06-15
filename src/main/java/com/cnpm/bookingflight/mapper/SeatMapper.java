package com.cnpm.bookingflight.mapper;

import com.cnpm.bookingflight.domain.Flight_Seat;
import com.cnpm.bookingflight.domain.Seat;
import com.cnpm.bookingflight.dto.request.SeatRequest;
import com.cnpm.bookingflight.dto.response.SeatResponse;
import com.cnpm.bookingflight.repository.Flight_SeatRepository;
import com.cnpm.bookingflight.repository.FlightRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Component
public class SeatMapper {

    final Flight_SeatRepository flightSeatRepository;
    final FlightRepository flightRepository;

    public Seat toSeat(SeatRequest request) {
        return Seat.builder()
                .seatCode(request.getSeatCode())
                .seatName(request.getSeatName())
                .price(request.getPrice())
                .description(request.getDescription())
                .build();
    }

    public SeatResponse toSeatResponse(Seat seat) {
        List<Flight_Seat> flightSeats = flightSeatRepository.findBySeatId(seat.getId());
        boolean hasActiveOrSoldOutFlight = flightSeats.stream()
                .map(fs -> flightRepository.findById(fs.getId().getFlightId()).orElse(null))
                .filter(f -> f != null)
                .anyMatch(f -> {
                    LocalDate currentDate = LocalDate.now();
                    boolean isPastDeparture = currentDate.isAfter(f.getDepartureDate());
                    List<Flight_Seat> flightSeatList = flightSeatRepository.findByIdFlightId(f.getId());
                    boolean hasRemainingTickets = flightSeatList != null && flightSeatList.stream().anyMatch(fs2 -> fs2.getRemainingTickets() > 0);
                    String status = isPastDeparture ? "Expired" : hasRemainingTickets ? "Active" : "Sold out";
                    return "Active".equals(status) || "Sold out".equals(status);
                });
        boolean canDelete = !hasActiveOrSoldOutFlight;
        return SeatResponse.builder()
                .id(seat.getId())
                .seatCode(seat.getSeatCode())
                .seatName(seat.getSeatName())
                .price(seat.getPrice())
                .canDelete(canDelete)
                .description(seat.getDescription())
                .build();
    }
}