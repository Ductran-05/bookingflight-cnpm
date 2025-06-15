package com.cnpm.bookingflight.service;

import com.cnpm.bookingflight.domain.Account;
import com.cnpm.bookingflight.domain.Flight;
import com.cnpm.bookingflight.domain.Flight_Airport;
import com.cnpm.bookingflight.domain.Flight_Seat;
import com.cnpm.bookingflight.domain.Parameters;
import com.cnpm.bookingflight.domain.Ticket;
import com.cnpm.bookingflight.dto.ResultPaginationDTO;
import com.cnpm.bookingflight.dto.request.FlightRequest;
import com.cnpm.bookingflight.dto.request.Flight_AirportRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.FlightCountResponse;
import com.cnpm.bookingflight.dto.response.FlightResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.FlightMapper;
import com.cnpm.bookingflight.mapper.Flight_AirportMapper;
import com.cnpm.bookingflight.mapper.Flight_SeatMapper;
import com.cnpm.bookingflight.mapper.ResultPaginationMapper;
import com.cnpm.bookingflight.repository.FlightRepository;
import com.cnpm.bookingflight.repository.Flight_AirportRepository;
import com.cnpm.bookingflight.repository.Flight_SeatRepository;
import com.cnpm.bookingflight.repository.ParametersRepository;
import com.cnpm.bookingflight.repository.TicketRepository;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlightService {

        final FlightRepository flightRepository;
        final FlightMapper flightMapper;
        final TicketRepository ticketRepository;
        final Flight_AirportMapper flightAirportMapper;
        final Flight_SeatMapper flightSeatMapper;
        final Flight_AirportRepository flightAirportRepository;
        final Flight_SeatRepository flightSeatRepository;
        final ResultPaginationMapper resultPaginationMapper;
        final ParametersRepository parametersRepository;
        final EmailService emailService;

        private void validateFlightRequest(@Valid FlightRequest request) {

                // Lấy thông số từ bảng Parameters
                Parameters parameters = parametersRepository.findById(1L)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

                // Tính thời gian bay
                LocalDateTime departureDateTime = LocalDateTime.of(request.getDepartureDate(),
                        request.getDepartureTime());
                LocalDateTime arrivalDateTime = LocalDateTime.of(request.getArrivalDate(), request.getArrivalTime());

                // Kiểm tra thời gian đến phải sau thời gian đi
                if (!arrivalDateTime.isAfter(departureDateTime)) {
                        throw new AppException(ErrorCode.INVALID_FLIGHT_DURATION,
                                "Arrival time must be after departure time");
                }

                // Kiểm tra thời gian bay tối thiểu
                long flightDurationMinutes = Duration.between(departureDateTime, arrivalDateTime).toMinutes();
                if (flightDurationMinutes < parameters.getMinFlightTime()) {
                        throw new AppException(ErrorCode.INVALID_FLIGHT_DURATION,
                                "Flight duration must be at least " + parameters.getMinFlightTime()
                                        + " minutes");
                }

                // Kiểm tra số lượng sân bay trung gian
                if (request.getInterAirports() != null
                        && request.getInterAirports().size() > parameters.getMaxInterQuantity()) {
                        throw new AppException(ErrorCode.INVALID_INTER_AIRPORTS,
                                "Number of intermediate airports cannot exceed "
                                        + parameters.getMaxInterQuantity());
                }

                // Kiểm tra sân bay đi và sân bay đến không trùng nhau
                if (request.getDepartureAirportId().equals(request.getArrivalAirportId())) {
                        throw new AppException(ErrorCode.INVALID_AIRPORT,
                                "Departure airport cannot be the same as arrival airport");
                }

                // Kiểm tra sân bay trung gian
                if (request.getInterAirports() != null && !request.getInterAirports().isEmpty()) {
                        // Tạo Set để kiểm tra trùng lặp sân bay trung gian
                        Set<Long> interAirportIds = new HashSet<>();
                        for (Flight_AirportRequest interAirport : request.getInterAirports()) {
                                Long airportId = interAirport.getAirportId();

                                // Kiểm tra sân bay trung gian không trùng với sân bay đi hoặc sân bay đến
                                if (airportId.equals(request.getDepartureAirportId()) || airportId.equals(request.getArrivalAirportId())) {
                                        throw new AppException(ErrorCode.INVALID_AIRPORT,
                                                "Intermediate airport cannot be the same as departure or arrival airport");
                                }

                                // Kiểm tra các sân bay trung gian không trùng nhau
                                if (!interAirportIds.add(airportId)) {
                                        throw new AppException(ErrorCode.INVALID_AIRPORT,
                                                "Duplicate intermediate airports are not allowed");
                                }

                                // Kiểm tra thời gian khởi hành phải sau thời gian đến tại sân bay trung gian
                                if (!interAirport.getDepartureDateTime().isAfter(interAirport.getArrivalDateTime())) {
                                        throw new AppException(ErrorCode.INVALID_STOP_DURATION,
                                                "Departure time at intermediate airport must be after arrival time");
                                }

                                // Kiểm tra thời gian dừng tại sân bay trung gian
                                long stopDurationMinutes = Duration.between(interAirport.getArrivalDateTime(),
                                        interAirport.getDepartureDateTime()).toMinutes();
                                if (stopDurationMinutes < parameters.getMinStopTime()
                                        || stopDurationMinutes > parameters.getMaxStopTime()) {
                                        throw new AppException(ErrorCode.INVALID_STOP_DURATION,
                                                "Stop duration must be between " + parameters.getMinStopTime()
                                                        + " and " + parameters.getMaxStopTime()
                                                        + " minutes");
                                }

                                // Kiểm tra thời gian đến của sân bay trung gian nằm trong khoảng thời gian bay
                                if (interAirport.getArrivalDateTime().isBefore(departureDateTime) ||
                                        interAirport.getArrivalDateTime().isAfter(arrivalDateTime) ||
                                        interAirport.getDepartureDateTime().isBefore(departureDateTime) ||
                                        interAirport.getDepartureDateTime().isAfter(arrivalDateTime)) {
                                        throw new AppException(ErrorCode.INVALID_STOP_DURATION,
                                                "Intermediate airport times must be between flight departure and arrival times");
                                }
                        }
                }
        }

        public ResponseEntity<APIResponse<ResultPaginationDTO>> getAllFlights(
                Specification<Flight> spec,
                Pageable pageable,
                List<Long> minPrice,
                List<Long> maxPrice,
                String from,
                String to,
                LocalDate arrivalDate,
                LocalDate departureDate,
                Boolean straight,
                List<Long> seats,
                List<Long> airlines) {

                Specification<Flight> finalSpec = Specification.where(spec)
                        .and((root, query, cb) -> cb.equal(root.get("isDeleted"), false)); // Lọc bỏ isDeleted = true
                List<Flight> flights = flightRepository.findAll(finalSpec, pageable).getContent();

                // Lọc
                if (minPrice != null && !minPrice.isEmpty()) {
                        long min = minPrice.get(0);
                        flights = flights.stream()
                                .filter(f -> f.getOriginalPrice() >= min)
                                .collect(Collectors.toList());
                }
                if (maxPrice != null && !maxPrice.isEmpty()) {
                        long max = maxPrice.get(0);
                        flights = flights.stream()
                                .filter(f -> f.getOriginalPrice() <= max)
                                .collect(Collectors.toList());
                }
                if (from != null) {
                        flights = flights.stream()
                                .filter(f -> f.getDepartureAirport().getCity().getCityName().equals(from))
                                .collect(Collectors.toList());
                }
                if (to != null) {
                        flights = flights.stream()
                                .filter(f -> f.getArrivalAirport().getCity().getCityName().equals(to))
                                .collect(Collectors.toList());
                }
                if (arrivalDate != null) {
                        flights = flights.stream()
                                .filter(f -> f.getArrivalDate().equals(arrivalDate))
                                .collect(Collectors.toList());
                }
                if (departureDate != null) {
                        flights = flights.stream()
                                .filter(f -> f.getDepartureDate().equals(departureDate))
                                .collect(Collectors.toList());
                }
                if (straight != null) {
                        flights = flights.stream()
                                .filter(f -> {
                                        Boolean isStraight = flightAirportRepository
                                                .countByFlightId(f.getId()) < 3;
                                        return isStraight == straight;
                                })
                                .collect(Collectors.toList());
                }
                if (seats != null && !seats.isEmpty()) {
                        flights = flights.stream()
                                .filter(f -> flightSeatRepository.existsByFlightIdAndSeatIdIn(f.getId(), seats))
                                .collect(Collectors.toList());
                }
                if (airlines != null && !airlines.isEmpty()) {
                        flights = flights.stream()
                                .filter(f -> airlines.contains(f.getPlane().getAirline().getId()))
                                .collect(Collectors.toList());
                }
                // Map sang DTO
                List<FlightResponse> responseList = flights.stream()
                        .map(flightMapper::toFlightResponse)
                        .collect(Collectors.toList());

                // Tạo Page<FlightResponse>
                Page<FlightResponse> responsePage = new PageImpl<>(
                        responseList, pageable, responseList.size());

                // Tạo kết quả phân trang
                ResultPaginationDTO result = resultPaginationMapper.toResultPagination(responsePage);

                // Gói API response
                APIResponse<ResultPaginationDTO> response = APIResponse.<ResultPaginationDTO>builder()
                        .status(200)
                        .message("Get all flights successfully")
                        .data(result)
                        .build();

                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<FlightResponse>> getFlightById(Long id) {
                Flight flight = flightRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
                if (flight.getIsDeleted()) {
                        throw new AppException(ErrorCode.NOT_FOUND);
                }
                boolean hasTickets = ticketRepository.existsByFlightId(id);
                FlightResponse flightResponse = flightMapper.toFlightResponse(flight)
                        .toBuilder()
                        .hasTickets(hasTickets)
                        .build();

                APIResponse<FlightResponse> response = APIResponse.<FlightResponse>builder()
                        .data(flightResponse)
                        .status(200)
                        .message("Get flight by id successfully")
                        .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<FlightResponse>> createFlight(@Valid FlightRequest request) {
                validateFlightRequest(request);

                Flight flight = flightMapper.toFlight(request);
                Flight savedFlight = flightRepository.save(flight);

                // Lưu các sân bay trung gian (Flight_Airport)
                if (request.getInterAirports() != null && !request.getInterAirports().isEmpty()) {
                        List<Flight_Airport> flightAirports = request.getInterAirports().stream()
                                .map(flightAirportRequest -> flightAirportMapper
                                        .toFlight_Airport(flightAirportRequest, savedFlight.getId()))
                                .collect(Collectors.toList());
                        flightAirportRepository.saveAll(flightAirports);
                }

                // Lưu các ghế (Flight_Seat)
                if (request.getSeats() != null && !request.getSeats().isEmpty()) {
                        List<Flight_Seat> flightSeats = request.getSeats().stream()
                                .map(flightSeatRequest -> flightSeatMapper.toFlight_Seat(flightSeatRequest,
                                        savedFlight.getId()))
                                .collect(Collectors.toList());
                        flightSeatRepository.saveAll(flightSeats);
                }

                FlightResponse flightResponse = flightMapper.toFlightResponse(savedFlight)
                        .toBuilder()
                        .hasTickets(false)
                        .build();

                APIResponse<FlightResponse> response = APIResponse.<FlightResponse>builder()
                        .data(flightResponse)
                        .status(201)
                        .message("Create flight successfully")
                        .build();
                return ResponseEntity.ok(response);
        }

        @Transactional
        public ResponseEntity<APIResponse<FlightResponse>> updateFlight(Long id, @Valid FlightRequest request) {
                validateFlightRequest(request);

                Flight flight = flightRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
                if (flight.getIsDeleted()) {
                        throw new AppException(ErrorCode.NOT_FOUND);
                }

                Flight updatedFlight = flightMapper.toFlight(request);
                updatedFlight.setId(id);
                Flight savedFlight = flightRepository.save(updatedFlight);

                flightAirportRepository.deleteByIdFlightId(id);
                if (request.getInterAirports() != null && !request.getInterAirports().isEmpty()) {
                        List<Flight_Airport> flightAirports = request.getInterAirports().stream()
                                .map(flightAirportRequest -> flightAirportMapper
                                        .toFlight_Airport(flightAirportRequest, savedFlight.getId()))
                                .collect(Collectors.toList());
                        flightAirportRepository.saveAll(flightAirports);
                }

                flightSeatRepository.deleteByIdFlightId(id);
                if (request.getSeats() != null && !request.getSeats().isEmpty()) {
                        List<Flight_Seat> flightSeats = request.getSeats().stream()
                                .map(flightSeatRequest -> flightSeatMapper.toFlight_Seat(flightSeatRequest,
                                        savedFlight.getId()))
                                .collect(Collectors.toList());
                        flightSeatRepository.saveAll(flightSeats);
                }

                // Kiểm tra nếu chuyến bay có vé đã đặt
                boolean hasTickets = ticketRepository.existsByFlightId(id);
                if (hasTickets) {
                        // Lấy danh sách vé của chuyến bay
                        List<Ticket> tickets = List.of(ticketRepository.findByFlightId(id));
                        for (Ticket ticket : tickets) {
                                try {
                                        // Tạo file PDF vé mới
                                        byte[] pdfBytes = generateTicketPdf(ticket, savedFlight);
                                        // Nội dung email thông báo
                                        String emailContent = String.format(
                                                "Dear %s,\n\n" +
                                                        "We would like to inform you that the details of your flight %s have been updated.\n" +
                                                        "Please find the updated ticket details in the attached PDF.\n\n" +
                                                        "If you have any questions, please contact us.\n" +
                                                        "Best regards,\nBookingFlight Team",
                                                ticket.getPassengerName(),
                                                savedFlight.getFlightCode()
                                        );
                                        // Gửi email với file PDF đính kèm
                                        emailService.sendWithAttachment(
                                                ticket.getPassengerEmail(),
                                                "Flight Update Notification - " + savedFlight.getFlightCode(),
                                                emailContent,
                                                "Updated_FlightTicket_" + ticket.getId() + ".pdf",
                                                pdfBytes
                                        );
                                } catch (Exception e) {
                                        System.err.println("Error sending email for ticket " + ticket.getId() + ": " + e.getMessage());
                                }
                        }
                }

                FlightResponse flightResponse = flightMapper.toFlightResponse(savedFlight)
                        .toBuilder()
                        .hasTickets(hasTickets)
                        .build();

                APIResponse<FlightResponse> response = APIResponse.<FlightResponse>builder()
                        .data(flightResponse)
                        .status(200)
                        .message("Update flight successfully")
                        .build();
                return ResponseEntity.ok(response);
        }

        private byte[] generateTicketPdf(Ticket ticket, Flight flight) throws Exception {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PdfWriter writer = new PdfWriter(baos);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                PdfFont font = PdfFontFactory.createFont("Helvetica", "WinAnsiEncoding");
                PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold", "WinAnsiEncoding");

                float[] columnWidths = {0.7f, 0.3f};
                Table mainTable = new Table(columnWidths);
                mainTable.setWidth(UnitValue.createPercentValue(100));
                mainTable.setBorder(new SolidBorder(1f));

                Table leftTable = new Table(3);
                leftTable.setWidth(UnitValue.createPercentValue(100));
                leftTable.setBorder(Border.NO_BORDER);

                Cell titleCell = new Cell(1, 3)
                        .add(new Paragraph("BOARDING PASS")
                                .setFont(boldFont)
                                .setFontSize(12)
                                .setHeight(20f)
                                .setTextAlignment(TextAlignment.CENTER))
                        .setBorder(Border.NO_BORDER);
                leftTable.addCell(titleCell);

                DeviceRgb blue = new DeviceRgb(38, 76, 230);
                leftTable.addCell(new Cell().add(new Paragraph(flight.getDepartureAirport().getCity().getCityCode())
                                .setFont(boldFont)
                                .setFontSize(20)
                                .setFontColor(blue)
                                .setTextAlignment(TextAlignment.CENTER))
                        .setBorder(Border.NO_BORDER));
                leftTable.addCell(new Cell().add(new Paragraph("")
                                .setHeight(20f))
                        .setBorder(Border.NO_BORDER));
                leftTable.addCell(new Cell().add(new Paragraph(flight.getArrivalAirport().getCity().getCityCode())
                                .setFont(boldFont)
                                .setFontSize(20)
                                .setFontColor(blue)
                                .setTextAlignment(TextAlignment.CENTER))
                        .setBorder(Border.NO_BORDER));

                leftTable.addCell(new Cell().add(new Paragraph(flight.getDepartureAirport().getCity().getCityName())
                                .setFont(font)
                                .setFontSize(12)
                                .setTextAlignment(TextAlignment.CENTER))
                        .setBorder(Border.NO_BORDER));
                leftTable.addCell(new Cell().add(new Paragraph("")
                                .setHeight(20f))
                        .setBorder(Border.NO_BORDER));
                leftTable.addCell(new Cell().add(new Paragraph(flight.getArrivalAirport().getCity().getCityName())
                                .setFont(font)
                                .setFontSize(12)
                                .setTextAlignment(TextAlignment.CENTER))
                        .setBorder(Border.NO_BORDER));

                Cell emptyRowCell = new Cell(1, 3)
                        .add(new Paragraph("")
                                .setHeight(10f))
                        .setBorder(Border.NO_BORDER);
                leftTable.addCell(emptyRowCell);

                leftTable.addCell(new Cell().add(new Paragraph(flight.getDepartureDate().toString())
                                .setFont(font)
                                .setFontSize(10)
                                .setTextAlignment(TextAlignment.CENTER))
                        .setBorder(Border.NO_BORDER));
                leftTable.addCell(new Cell().add(new Paragraph(""))
                        .setBorder(Border.NO_BORDER));
                leftTable.addCell(new Cell().add(new Paragraph(flight.getArrivalDate().toString())
                                .setFont(font)
                                .setFontSize(10)
                                .setTextAlignment(TextAlignment.CENTER))
                        .setBorder(Border.NO_BORDER));

                leftTable.addCell(new Cell().add(new Paragraph(flight.getDepartureTime().toString())
                                .setFont(font)
                                .setFontSize(10)
                                .setTextAlignment(TextAlignment.CENTER))
                        .setBorder(Border.NO_BORDER));
                leftTable.addCell(new Cell().add(new Paragraph(""))
                        .setBorder(Border.NO_BORDER));
                leftTable.addCell(new Cell().add(new Paragraph(flight.getArrivalTime().toString())
                                .setFont(font)
                                .setFontSize(10)
                                .setTextAlignment(TextAlignment.CENTER))
                        .setBorder(Border.NO_BORDER));

                leftTable.addCell(new Cell().add(new Paragraph("")
                                .setHeight(15f))
                        .setBorder(Border.NO_BORDER));
                leftTable.addCell(new Cell().add(new Paragraph("")
                                .setHeight(15f))
                        .setBorder(Border.NO_BORDER));
                leftTable.addCell(new Cell().add(new Paragraph("")
                                .setHeight(15f))
                        .setBorder(Border.NO_BORDER));

                Cell passengerCell = new Cell(1, 2)
                        .add(new Paragraph()
                                .add(new com.itextpdf.layout.element.Text("Passenger: ").setFont(boldFont))
                                .add(new com.itextpdf.layout.element.Text(ticket.getPassengerName()).setFont(font))
                                .setFontSize(10))
                        .setBorder(Border.NO_BORDER);
                leftTable.addCell(passengerCell);
                leftTable.addCell(new Cell().add(new Paragraph()
                                .add(new com.itextpdf.layout.element.Text("Flight: ").setFont(boldFont))
                                .add(new com.itextpdf.layout.element.Text(flight.getFlightCode()).setFont(font))
                                .setFontSize(10))
                        .setBorder(Border.NO_BORDER));

                mainTable.addCell(new Cell().add(leftTable).setBorder(Border.NO_BORDER));

                DeviceRgb lightBlue = new DeviceRgb(219, 234, 254);
                Table rightTable = new Table(1);
                rightTable.setWidth(UnitValue.createPercentValue(100));
                rightTable.setBorder(Border.NO_BORDER);

                rightTable.addCell(new Cell().add(new Paragraph(flight.getPlane().getAirline().getAirlineName())
                                .setFont(font)
                                .setFontSize(12)
                                .setTextAlignment(TextAlignment.CENTER))
                        .setBackgroundColor(lightBlue)
                        .setBorder(Border.NO_BORDER));
                rightTable.addCell(new Cell().add(new Paragraph("")
                                .setHeight(20f))
                        .setBackgroundColor(lightBlue)
                        .setBorder(Border.NO_BORDER));
                rightTable.addCell(new Cell().add(new Paragraph()
                                .add(new com.itextpdf.layout.element.Text("\tTicketCode: ").setFont(boldFont))
                                .add(new com.itextpdf.layout.element.Text(ticket.getTicketCode()).setFont(font))
                                .setFontSize(10))
                        .setBackgroundColor(lightBlue)
                        .setBorder(Border.NO_BORDER));
                rightTable.addCell(new Cell().add(new Paragraph()
                                .add(new com.itextpdf.layout.element.Text("\tPassenger: ").setFont(boldFont))
                                .add(new com.itextpdf.layout.element.Text(ticket.getPassengerName()).setFont(font))
                                .setFontSize(10))
                        .setBackgroundColor(lightBlue)
                        .setBorder(Border.NO_BORDER));
                rightTable.addCell(new Cell().add(new Paragraph()
                                .add(new com.itextpdf.layout.element.Text("\tSeat: ").setFont(boldFont))
                                .add(new com.itextpdf.layout.element.Text(ticket.getSeat().getSeatName()).setFont(font))
                                .setFontSize(10))
                        .setBackgroundColor(lightBlue)
                        .setBorder(Border.NO_BORDER));
                rightTable.addCell(new Cell().add(new Paragraph()
                                .add(new com.itextpdf.layout.element.Text("\tDate: ").setFont(boldFont))
                                .add(new com.itextpdf.layout.element.Text(flight.getDepartureDate().toString()).setFont(font))
                                .setFontSize(10))
                        .setBackgroundColor(lightBlue)
                        .setBorder(Border.NO_BORDER));
                rightTable.addCell(new Cell().add(new Paragraph()
                                .add(new com.itextpdf.layout.element.Text("\tBoarding: ").setFont(boldFont))
                                .add(new com.itextpdf.layout.element.Text(flight.getDepartureTime().toString()).setFont(font))
                                .setFontSize(10))
                        .setBackgroundColor(lightBlue)
                        .setBorder(Border.NO_BORDER));
                rightTable.addCell(new Cell().add(new Paragraph("")
                                .setHeight(20f))
                        .setBackgroundColor(lightBlue)
                        .setBorder(Border.NO_BORDER));

                Cell rightCell = new Cell().add(rightTable).setBackgroundColor(lightBlue).setBorder(Border.NO_BORDER);
                mainTable.addCell(rightCell);

                document.add(mainTable);

                Paragraph footer = new Paragraph("Thank you for choosing BookingFlight!")
                        .setFont(font)
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginTop(20);
                document.add(footer);

                document.close();
                return baos.toByteArray();
        }

        public ResponseEntity<APIResponse<Void>> deleteFlightById(Long id) {
                Flight flight = flightRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
                if (flight.getIsDeleted()) {
                        throw new AppException(ErrorCode.NOT_FOUND);
                }

                // Kiểm tra nếu chuyến bay có vé đã đặt
                boolean hasTickets = ticketRepository.existsByFlightId(id);
                if (hasTickets) {
                        // Lấy danh sách vé của chuyến bay
                        List<Ticket> tickets = List.of(ticketRepository.findByFlightId(id));
                        for (Ticket ticket : tickets) {
                                try {
                                        // Nội dung email thông báo hủy chuyến bay
                                        String emailContent = String.format(
                                                "Dear %s,\n\n" +
                                                        "We regret to inform you that your flight %s has been canceled.\n" +
                                                        "Flight Details:\n" +
                                                        "- Flight Code: %s\n" +
                                                        "- From: %s\n" +
                                                        "- To: %s\n" +
                                                        "- Departure Time: %s\n" +
                                                        "We apologize for any inconvenience caused. Please contact our support team for assistance with refunds or alternative flight arrangements.\n" +
                                                        "Best regards,\nBookingFlight Team",
                                                ticket.getPassengerName(),
                                                flight.getFlightCode(),
                                                flight.getFlightCode(),
                                                flight.getDepartureAirport().getCity().getCityName(),
                                                flight.getArrivalAirport().getCity().getCityName(),
                                                LocalDateTime.of(flight.getDepartureDate(), flight.getDepartureTime())
                                        );
                                        // Gửi email thông báo
                                        emailService.send(
                                                ticket.getPassengerEmail(),
                                                emailContent,
                                                "Flight Cancellation Notice"
                                        );
                                } catch (Exception e) {
                                        System.err.println("Error sending cancellation email for ticket " + ticket.getId() + ": " + e.getMessage());
                                }
                        }
                }

                // Đặt isDeleted thành true
                flight.setIsDeleted(true);
                flightRepository.save(flight);

                APIResponse<Void> response = APIResponse.<Void>builder()
                        .status(204)
                        .message("Delete flight successfully")
                        .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<FlightCountResponse>> getFlightCount(String period) {
                LocalDate currentDate = LocalDate.now();
                int currentYear = currentDate.getYear();
                int currentMonth = currentDate.getMonthValue();

                FlightCountResponse flightCountResponse = new FlightCountResponse();
                flightCountResponse.setPeriodType(period);

                switch (period.toLowerCase()) {
                        case "month":
                                long currentMonthCount = flightRepository.countFlightsByMonth(currentYear,
                                        currentMonth);
                                long previousMonthCount = flightRepository.countFlightsByMonth(
                                        currentMonth == 1 ? currentYear - 1 : currentYear,
                                        currentMonth == 1 ? 12 : currentMonth - 1);
                                flightCountResponse.setCurrentPeriodCount(currentMonthCount);
                                flightCountResponse.setPreviousPeriodCount(previousMonthCount);
                                break;

                        case "year":
                                long currentYearCount = flightRepository.countFlightsByYear(currentYear);
                                long previousYearCount = flightRepository.countFlightsByYear(currentYear - 1);
                                flightCountResponse.setCurrentPeriodCount(currentYearCount);
                                flightCountResponse.setPreviousPeriodCount(previousYearCount);
                                break;

                        default:
                                throw new AppException(ErrorCode.INVALID_PERIOD_TYPE);
                }

                APIResponse<FlightCountResponse> response = APIResponse.<FlightCountResponse>builder()
                        .status(200)
                        .message("Get flight count successfully")
                        .data(flightCountResponse)
                        .build();
                return ResponseEntity.ok(response);
        }
}