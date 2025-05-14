package com.cnpm.bookingflight.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.cnpm.bookingflight.domain.Airport;
import com.cnpm.bookingflight.domain.Flight;
import com.cnpm.bookingflight.domain.Flight_Airport;
import com.cnpm.bookingflight.domain.Flight_Seat;
import com.cnpm.bookingflight.domain.Plane;
import com.cnpm.bookingflight.dto.request.FlightRequest;
import com.cnpm.bookingflight.dto.response.AirportResponse;
import com.cnpm.bookingflight.dto.response.FlightResponse;
import com.cnpm.bookingflight.dto.response.Flight_AirportResponse;
import com.cnpm.bookingflight.dto.response.PlaneResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.repository.AirportRepository;
import com.cnpm.bookingflight.repository.Flight_AirportRepository;
import com.cnpm.bookingflight.repository.Flight_SeatRepository;
import com.cnpm.bookingflight.repository.PlaneRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class FlightMapper {
        final Flight_AirportRepository flight_AirportRepository;
        final Flight_SeatRepository flight_SeatRepository;
        final AirportRepository airportRepository;
        final PlaneRepository planeRepository;

        public FlightResponse toFlightResponse(Flight flight) {
                List<Flight_Airport> interAirports = flight_AirportRepository.findByIdFlightId(flight.getId());
                List<Flight_Seat> seats = flight_SeatRepository.findByIdFlightId(flight.getId());
                return FlightResponse.builder()
                        .id(flight.getId())
                        .flightCode(flight.getFlightCode())
                        .plane(convertPlane(flight.getPlane()))
                        .departureAirport(convertAirport(flight.getDepartureAirport()))
                        .arrivalAirport(convertAirport(flight.getArrivalAirport()))
                        .departureDate(flight.getDepartureDate())
                        .arrivalDate(flight.getArrivalDate())
                        .departureTime(flight.getDepartureTime())
                        .arrivalTime(flight.getArrivalTime())
                        .originalPrice(flight.getOriginalPrice())
                        .interAirports(convertFlight_Airport(interAirports))
                        .seats(convertFlight_Seat(seats))
                        .build();
        }

        public List<FlightResponse> toFlightResponseList(List<Flight> flights) {
                return flights.stream()
                        .map(this::toFlightResponse)
                        .toList();
        }

        public Flight toFlight(FlightRequest request) {
                return Flight.builder()
                        .flightCode(request.getFlightCode())
                        .plane(planeRepository.findById(request.getPlaneId())
                                .orElseThrow(() -> new AppException(ErrorCode.INVALID)))
                        .departureAirport(airportRepository.findById(request.getDepartureAirportId())
                                .orElseThrow(() -> new AppException(ErrorCode.INVALID)))
                        .arrivalAirport(airportRepository.findById(request.getArrivalAirportId())
                                .orElseThrow(() -> new AppException(ErrorCode.INVALID)))
                        .departureDate(request.getDepartureDate())
                        .arrivalDate(request.getArrivalDate())
                        .departureTime(request.getDepartureTime())
                        .arrivalTime(request.getArrivalTime())
                        .originalPrice(request.getOriginPrice())
                        .build();
        }

        public PlaneResponse convertPlane(Plane plane) {
                return PlaneResponse.builder()
                        .id(plane.getId())
                        .planeCode(plane.getPlaneCode())
                        .planeName(plane.getPlaneName())
                        .airlineName(plane.getAirline() != null ? plane.getAirline().getAirlineName() : null)
                        .build();
        }

        public AirportResponse convertAirport(Airport airport) {
                return AirportResponse.builder()
                        .id(airport.getId())
                        .airportCode(airport.getAirportCode())
                        .airportName(airport.getAirportName())
                        .cityName(airport.getCity() != null ? airport.getCity().getCityName() : null)
                        .build();
        }

        public List<Flight_AirportResponse> convertFlight_Airport(List<Flight_Airport> request) {
                List<Flight_AirportResponse> results = new ArrayList<>();
                for (Flight_Airport item : request) {
                        results.add(Flight_AirportResponse.builder()
                                .airport(convertAirport(item.getAirport()))
                                .departureDateTime(item.getDepartureDateTime())
                                .arrivalDateTime(item.getArrivalDateTime())
                                .note(item.getNote())
                                .build());
                }
                return results;
        }

        public List<Flight_Seat> convertFlight_Seat(List<Flight_Seat> request) {
                List<Flight_Seat> results = new ArrayList<>();
                for (Flight_Seat item : request) {
                        results.add(Flight_Seat.builder()
                                .seat(item.getSeat())
                                .quantity(item.getQuantity())
                                .remainingTickets(item.getRemainingTickets())
                                .price(item.getPrice())
                                .build());
                }
                return results;
        }
}