package com.cnpm.bookingflight.service;

import com.cnpm.bookingflight.domain.*;
import com.cnpm.bookingflight.domain.id.Flight_SeatId;
import com.cnpm.bookingflight.dto.ResultPaginationDTO;
import com.cnpm.bookingflight.dto.request.TicketRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.BookingRateResponse;
import com.cnpm.bookingflight.dto.response.RevenueResponse;
import com.cnpm.bookingflight.dto.response.TicketResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.ResultPaginationMapper;
import com.cnpm.bookingflight.mapper.TicketMapper;
import com.cnpm.bookingflight.repository.*;
import com.itextpdf.layout.properties.BorderCollapsePropertyValue;
import com.itextpdf.layout.properties.Property;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketService {

    final TicketRepository ticketRepository;
    final TicketMapper ticketMapper;
    final Flight_SeatRepository flight_SeatRepository;
    final ResultPaginationMapper resultPaginationMapper;
    final FlightRepository flightRepository;
    final ParametersRepository parametersRepository;
    final EmailService emailService;
    final AccountRepository accountRepository;

    public ResponseEntity<APIResponse<ResultPaginationDTO>> getAllTickets(Specification<Ticket> spec,
                                                                          Pageable pageable) {
        Specification<Ticket> finalSpec = Specification.where(spec)
                .and((root, query, cb) -> cb.equal(root.get("isDeleted"), false));
        ResultPaginationDTO result = resultPaginationMapper
                .toResultPagination(ticketRepository.findAll(finalSpec, pageable).map(ticketMapper::toTicketResponse));
        APIResponse<ResultPaginationDTO> response = APIResponse.<ResultPaginationDTO>builder()
                .status(200)
                .message("Get all tickets successfully")
                .data(result)
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<TicketResponse>> getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        if (ticket.getIsDeleted()) {
            throw new AppException(ErrorCode.NOT_FOUND);
        }
        APIResponse<TicketResponse> response = APIResponse.<TicketResponse>builder()
                .data(ticketMapper.toTicketResponse(ticket))
                .status(200)
                .message("Get ticket by id successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<List<TicketResponse>>> bookingTicket(@Valid TicketRequest request) {
        Parameters parameters = parametersRepository.findById(1L)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        Flight flight = flightRepository.findById(request.getFlightId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        LocalDateTime departureDateTime = LocalDateTime.of(flight.getDepartureDate(), flight.getDepartureTime());
        LocalDateTime bookingDeadline = departureDateTime.minusDays(parameters.getLatestBookingDay());
        LocalDateTime currentTime = LocalDateTime.now();
        if (currentTime.isAfter(bookingDeadline)) {
            throw new AppException(ErrorCode.INVALID_BOOKING_TIME,
                    "Booking must be made at least " + parameters.getLatestBookingDay() + " day(s) before departure");
        }

        Map<Long, Integer> seatQuantities = new HashMap<>();
        for (TicketRequest.TicketInfo ticketInfo : request.getTickets()) {
            seatQuantities.merge(ticketInfo.getSeatId(), 1, Integer::sum);
        }

        for (Map.Entry<Long, Integer> entry : seatQuantities.entrySet()) {
            Long seatId = entry.getKey();
            int quantity = entry.getValue();
            Flight_Seat flightSeat = flight_SeatRepository.findById(new Flight_SeatId(request.getFlightId(), seatId))
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
            if (flightSeat.getRemainingTickets() < quantity) {
                throw new AppException(ErrorCode.OUT_OF_TICKETS,
                        "Not enough tickets available for seat ID " + seatId);
            }
        }

        List<Ticket> tickets = new ArrayList<>();
        for (TicketRequest.TicketInfo ticketInfo : request.getTickets()) {
            Flight_Seat flightSeat = flight_SeatRepository.findById(new Flight_SeatId(request.getFlightId(), ticketInfo.getSeatId()))
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
            flightSeat.setRemainingTickets(flightSeat.getRemainingTickets() - 1);
            flight_SeatRepository.save(flightSeat);

            Ticket ticket = ticketMapper.toTicket(ticketInfo, request.getFlightId(), null);
            tickets.add(ticket);
        }

        List<Ticket> savedTickets = ticketRepository.saveAll(tickets);

        APIResponse<List<TicketResponse>> response = APIResponse.<List<TicketResponse>>builder()
                .status(201)
                .message("Booking tickets successfully")
                .data(ticketMapper.toTicketResponseList(savedTickets))
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<List<TicketResponse>>> bookingTicketWithAuth(@Valid TicketRequest request, Long userId) {
        Parameters parameters = parametersRepository.findById(1L)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        Flight flight = flightRepository.findById(request.getFlightId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        LocalDateTime departureDateTime = LocalDateTime.of(flight.getDepartureDate(), flight.getDepartureTime());
        LocalDateTime bookingDeadline = departureDateTime.minusDays(parameters.getLatestBookingDay());
        LocalDateTime currentTime = LocalDateTime.now();
        if (currentTime.isAfter(bookingDeadline)) {
            throw new AppException(ErrorCode.INVALID_BOOKING_TIME,
                    "Booking must be made at least " + parameters.getLatestBookingDay() + " day(s) before departure");
        }

        Map<Long, Integer> seatQuantities = new HashMap<>();
        for (TicketRequest.TicketInfo ticketInfo : request.getTickets()) {
            seatQuantities.merge(ticketInfo.getSeatId(), 1, Integer::sum);
        }

        for (Map.Entry<Long, Integer> entry : seatQuantities.entrySet()) {
            Long seatId = entry.getKey();
            int quantity = entry.getValue();
            Flight_Seat flightSeat = flight_SeatRepository.findById(new Flight_SeatId(request.getFlightId(), seatId))
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
            if (flightSeat.getRemainingTickets() < quantity) {
                throw new AppException(ErrorCode.OUT_OF_TICKETS,
                        "Not enough tickets available for seat ID " + seatId);
            }
        }

        List<Ticket> tickets = new ArrayList<>();
        for (TicketRequest.TicketInfo ticketInfo : request.getTickets()) {
            Flight_Seat flightSeat = flight_SeatRepository.findById(new Flight_SeatId(request.getFlightId(), ticketInfo.getSeatId()))
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
            flightSeat.setRemainingTickets(flightSeat.getRemainingTickets() - 1);
            flight_SeatRepository.save(flightSeat);

            Ticket ticket = ticketMapper.toTicket(ticketInfo, request.getFlightId(), userId);
            tickets.add(ticket);
        }

        List<Ticket> savedTickets = ticketRepository.saveAll(tickets);

        for (Ticket ticket : savedTickets) {
            try {
                byte[] pdfBytes = generateTicketPdf(ticket);
                String emailContent = String.format(
                        "Dear %s,\n\n" +
                                "Thank you for booking a flight with BookingFlight!\n" +
                                "Your booking has been successfully confirmed. Please find the ticket details in the attached PDF.\n\n" +
                                "Ticket Information:\n" +
                                "- Flight Code: %s\n" +
                                "- Passenger: %s\n" +
                                "- From: %s\n" +
                                "- To: %s\n" +
                                "- Departure Time: %s\n\n" +
                                "If you have any questions, please contact us.\n" +
                                "Best regards,\nBookingFlight Team",
                        ticket.getPassengerName(),
                        ticket.getFlight().getFlightCode(),
                        ticket.getPassengerName(),
                        ticket.getFlight().getDepartureAirport().getCity().getCityName(),
                        ticket.getFlight().getArrivalAirport().getCity().getCityName(),
                        departureDateTime
                );
                emailService.sendWithAttachment(
                        ticket.getPassengerEmail(),
                        "Flight Booking Confirmation - " + ticket.getFlight().getFlightCode(),
                        emailContent,
                        "FlightTicket_" + ticket.getId() + ".pdf",
                        pdfBytes
                );
            } catch (Exception e) {
                System.err.println("Error sending email for ticket " + ticket.getId() + ": " + e.getMessage());
            }
        }

        APIResponse<List<TicketResponse>> response = APIResponse.<List<TicketResponse>>builder()
                .status(201)
                .message("Booking tickets successfully")
                .data(ticketMapper.toTicketResponseList(savedTickets))
                .build();
        return ResponseEntity.ok(response);
    }

    private byte[] generateTicketPdf(Ticket ticket) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        PdfFont font = PdfFontFactory.createFont("Helvetica", "WinAnsiEncoding");
        PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold", "WinAnsiEncoding");

        float[] columnWidths = {0.7f, 0.3f};
        Table mainTable = new Table(columnWidths);
        mainTable.setWidth(UnitValue.createPercentValue(100));
        mainTable.setBorder(new SolidBorder(1f)); // Keep only the outer border

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
        leftTable.addCell(new Cell().add(new Paragraph(ticket.getFlight().getDepartureAirport().getCity().getCityCode())
                        .setFont(boldFont)
                        .setFontSize(20)
                        .setFontColor(blue)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBorder(Border.NO_BORDER));
        leftTable.addCell(new Cell().add(new Paragraph("")
                        .setHeight(20f))
                .setBorder(Border.NO_BORDER));
        leftTable.addCell(new Cell().add(new Paragraph(ticket.getFlight().getArrivalAirport().getCity().getCityCode())
                        .setFont(boldFont)
                        .setFontSize(20)
                        .setFontColor(blue)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBorder(Border.NO_BORDER));

        leftTable.addCell(new Cell().add(new Paragraph(ticket.getFlight().getDepartureAirport().getCity().getCityName())
                        .setFont(font)
                        .setFontSize(12)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBorder(Border.NO_BORDER));
        leftTable.addCell(new Cell().add(new Paragraph("")
                        .setHeight(20f))
                .setBorder(Border.NO_BORDER));
        leftTable.addCell(new Cell().add(new Paragraph(ticket.getFlight().getArrivalAirport().getCity().getCityName())
                        .setFont(font)
                        .setFontSize(12)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBorder(Border.NO_BORDER));

        Cell emptyRowCell = new Cell(1, 3)
                .add(new Paragraph("")
                        .setHeight(10f))
                .setBorder(Border.NO_BORDER);
        leftTable.addCell(emptyRowCell);

        leftTable.addCell(new Cell().add(new Paragraph(ticket.getFlight().getDepartureDate().toString())
                        .setFont(font)
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBorder(Border.NO_BORDER));
        leftTable.addCell(new Cell().add(new Paragraph(""))
                .setBorder(Border.NO_BORDER));
        leftTable.addCell(new Cell().add(new Paragraph(ticket.getFlight().getArrivalDate().toString())
                        .setFont(font)
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBorder(Border.NO_BORDER));

        leftTable.addCell(new Cell().add(new Paragraph(ticket.getFlight().getDepartureTime().toString())
                        .setFont(font)
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBorder(Border.NO_BORDER));
        leftTable.addCell(new Cell().add(new Paragraph(""))
                .setBorder(Border.NO_BORDER));
        leftTable.addCell(new Cell().add(new Paragraph(ticket.getFlight().getArrivalTime().toString())
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
                        .add(new com.itextpdf.layout.element.Text(ticket.getFlight().getFlightCode()).setFont(font))
                        .setFontSize(10))
                .setBorder(Border.NO_BORDER));

        // Add leftTable to mainTable without border
        mainTable.addCell(new Cell().add(leftTable).setBorder(Border.NO_BORDER));

        DeviceRgb lightBlue = new DeviceRgb(219, 234, 254);
        Table rightTable = new Table(1);
        rightTable.setWidth(UnitValue.createPercentValue(100));
        rightTable.setBorder(Border.NO_BORDER);

        rightTable.addCell(new Cell().add(new Paragraph(ticket.getFlight().getPlane().getAirline().getAirlineName())
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
        rightTable.addCell(new Cell().add(new Paragraph("")
                        .setHeight(20f))
                .setBackgroundColor(lightBlue)
                .setBorder(Border.NO_BORDER));
        rightTable.addCell(new Cell().add(new Paragraph()
                        .add(new com.itextpdf.layout.element.Text("\tDate: ").setFont(boldFont))
                        .add(new com.itextpdf.layout.element.Text(ticket.getFlight().getDepartureDate().toString()).setFont(font))
                        .setFontSize(10))
                .setBackgroundColor(lightBlue)
                .setBorder(Border.NO_BORDER));
        rightTable.addCell(new Cell().add(new Paragraph()
                        .add(new com.itextpdf.layout.element.Text("\tBoarding: ").setFont(boldFont))
                        .add(new com.itextpdf.layout.element.Text(ticket.getFlight().getDepartureTime().toString()).setFont(font))
                        .setFontSize(10))
                .setBackgroundColor(lightBlue)
                .setBorder(Border.NO_BORDER));
        rightTable.addCell(new Cell().add(new Paragraph("")
                        .setHeight(20f))
                .setBackgroundColor(lightBlue)
                .setBorder(Border.NO_BORDER));

        // Add rightTable to mainTable without border
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

    public ResponseEntity<APIResponse<TicketResponse>> updateTicket(Long id, @Valid TicketRequest request) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        if (ticket.getIsDeleted()) {
            throw new AppException(ErrorCode.NOT_FOUND);
        }

        if (request.getTickets() == null || request.getTickets().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_TICKET_INFO);
        }
        TicketRequest.TicketInfo ticketInfo = request.getTickets().get(0);

        if (!ticket.getSeat().getId().equals(ticketInfo.getSeatId())) {
            Flight_Seat oldFlightSeat = flight_SeatRepository.findById(new Flight_SeatId(ticket.getFlight().getId(), ticket.getSeat().getId()))
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
            oldFlightSeat.setRemainingTickets(oldFlightSeat.getRemainingTickets() + 1);
            flight_SeatRepository.save(oldFlightSeat);

            Flight_Seat newFlightSeat = flight_SeatRepository.findById(new Flight_SeatId(request.getFlightId(), ticketInfo.getSeatId()))
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
            if (newFlightSeat.getRemainingTickets() < 1) {
                throw new AppException(ErrorCode.OUT_OF_TICKETS,
                        "Not enough tickets available for seat ID " + ticketInfo.getSeatId());
            }
            newFlightSeat.setRemainingTickets(newFlightSeat.getRemainingTickets() - 1);
            flight_SeatRepository.save(newFlightSeat);
        }

        Ticket updatedTicket = ticketMapper.updateTicket(ticket, request);
        APIResponse<TicketResponse> response = APIResponse.<TicketResponse>builder()
                .status(200)
                .message("Update ticket successfully")
                .data(ticketMapper.toTicketResponse(ticketRepository.save(updatedTicket)))
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<RevenueResponse>> getRevenue(String period) {
        LocalDate currentDate = LocalDate.now();
        int currentYear = currentDate.getYear();
        int currentMonth = currentDate.getMonthValue();

        RevenueResponse revenueResponse = new RevenueResponse();
        revenueResponse.setPeriodType(period);

        switch (period.toLowerCase()) {
            case "month":
                double currentMonthRevenue = ticketRepository.calculateRevenueByMonth(currentYear, currentMonth);
                double previousMonthRevenue = ticketRepository.calculateRevenueByMonth(
                        currentMonth == 1 ? currentYear - 1 : currentYear,
                        currentMonth == 1 ? 12 : currentMonth - 1
                );
                revenueResponse.setCurrentPeriodRevenue(currentMonthRevenue);
                revenueResponse.setPreviousPeriodRevenue(previousMonthRevenue);
                break;

            case "year":
                double currentYearRevenue = ticketRepository.calculateRevenueByYear(currentYear);
                double previousYearRevenue = ticketRepository.calculateRevenueByYear(currentYear - 1);
                revenueResponse.setCurrentPeriodRevenue(currentYearRevenue);
                revenueResponse.setPreviousPeriodRevenue(previousYearRevenue);
                break;

            default:
                throw new AppException(ErrorCode.INVALID_PERIOD_TYPE);
        }

        APIResponse<RevenueResponse> response = APIResponse.<RevenueResponse>builder()
                .status(200)
                .message("Get revenue successfully")
                .data(revenueResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<BookingRateResponse>> getBookingRate() {
        int currentYear = LocalDate.now().getYear();
        BookingRateResponse bookingRateResponse = new BookingRateResponse();
        bookingRateResponse.setYear(currentYear);
        List<BookingRateResponse.MonthlyBooking> monthlyBookings = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            long soldTickets = ticketRepository.countTicketsByMonth(currentYear, month);
            long issuedTickets = ticketRepository.sumFlightSeatQuantityByMonth(currentYear, month);
            monthlyBookings.add(new BookingRateResponse.MonthlyBooking(month, soldTickets, issuedTickets));
        }

        bookingRateResponse.setMonthlyBookings(monthlyBookings);

        APIResponse<BookingRateResponse> response = APIResponse.<BookingRateResponse>builder()
                .status(200)
                .message("Get booking rate successfully")
                .data(bookingRateResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Void>> deleteTicket(Long ticketId) {
        Parameters parameters = parametersRepository.findById(1L)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        if (ticket.getIsDeleted()) {
            throw new AppException(ErrorCode.NOT_FOUND);
        }
        Flight flight = flightRepository.findById(ticket.getFlight().getId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        Flight_Seat flightSeat = flight_SeatRepository.findById(new Flight_SeatId(flight.getId(), ticket.getSeat().getId()))
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        LocalDateTime departureDateTime = LocalDateTime.of(
                flight.getDepartureDate(),
                flight.getDepartureTime()
        );
        LocalDate earliestRefundDate = departureDateTime.minusDays(parameters.getLatestCancelDay()).toLocalDate();
        if (LocalDate.now().isAfter(earliestRefundDate)) {
            throw new AppException(ErrorCode.CANNOT_REFUND,
                    "Cannot refund ticket as it is too close to departure time");
        }

        int refundedAmount = flightSeat.getPrice();
        flightSeat.setRemainingTickets(flightSeat.getRemainingTickets() + 1);
        flight_SeatRepository.save(flightSeat);

        ticket.setIsDeleted(true);
        ticketRepository.save(ticket);

        String emailContent = String.format(
                "Dear %s,\n\n" +
                        "Your ticket for flight %s has been successfully refunded.\n" +
                        "Flight details:\n" +
                        "- From: %s\n" +
                        "- To: %s\n" +
                        "- Departure time: %s\n" +
                        "- Seat class: %s\n" +
                        "- Refunded amount: %d VND\n\n" +
                        "Thank you for using our services!\n" +
                        "Best regards,\nBookingFlight Team",
                ticket.getPassengerName(),
                flight.getFlightCode(),
                flight.getDepartureAirport().getAirportName(),
                flight.getArrivalAirport().getAirportName(),
                departureDateTime,
                ticket.getSeat().getSeatName(),
                refundedAmount
        );
        emailService.send(ticket.getPassengerEmail(), emailContent);

        APIResponse<Void> response = APIResponse.<Void>builder()
                .status(200)
                .message("Ticket refunded successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Void>> deleteAuthTicket(Long ticketId, Long userId) {
        Parameters parameters = parametersRepository.findById(1L)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        if (ticket.getIsDeleted()) {
            throw new AppException(ErrorCode.NOT_FOUND);
        }
        if (ticket.getUserBooking() == null || !ticket.getUserBooking().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "You are not authorized to delete this ticket");
        }

        Flight flight = flightRepository.findById(ticket.getFlight().getId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        Flight_Seat flightSeat = flight_SeatRepository.findById(new Flight_SeatId(flight.getId(), ticket.getSeat().getId()))
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        LocalDateTime departureDateTime = LocalDateTime.of(
                flight.getDepartureDate(),
                flight.getDepartureTime()
        );
        LocalDate earliestRefundDate = departureDateTime.minusDays(parameters.getLatestCancelDay()).toLocalDate();
        if (LocalDate.now().isAfter(earliestRefundDate)) {
            throw new AppException(ErrorCode.CANNOT_REFUND,
                    "Cannot refund ticket as it is too close to departure time");
        }

        int refundedAmount = flightSeat.getPrice();
        flightSeat.setRemainingTickets(flightSeat.getRemainingTickets() + 1);
        flight_SeatRepository.save(flightSeat);

        ticket.setIsDeleted(true);
        ticketRepository.save(ticket);

        String emailContent = String.format(
                "Dear %s,\n\n" +
                        "Your ticket for flight %s has been successfully refunded.\n" +
                        "Flight details:\n" +
                        "- From: %s\n" +
                        "- To: %s\n" +
                        "- Departure time: %s\n" +
                        "- Seat class: %s\n" +
                        "- Refunded amount: %d VND\n\n" +
                        "Thank you for using our services!\n" +
                        "Best regards,\nBookingFlight Team",
                ticket.getPassengerName(),
                flight.getFlightCode(),
                flight.getDepartureAirport().getAirportName(),
                flight.getArrivalAirport().getAirportName(),
                departureDateTime,
                ticket.getSeat().getSeatName(),
                refundedAmount
        );
        emailService.send(ticket.getPassengerEmail(), emailContent);

        APIResponse<Void> response = APIResponse.<Void>builder()
                .status(200)
                .message("Ticket refunded and deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<List<TicketResponse>>> getUserTickets(String username) {
        Account user = accountRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        List<Ticket> tickets = ticketRepository.findByUserBooking(user).stream()
                .filter(ticket -> !ticket.getIsDeleted())
                .toList();
        List<TicketResponse> ticketResponses = ticketMapper.toTicketResponseList(tickets);
        APIResponse<List<TicketResponse>> response = APIResponse.<List<TicketResponse>>builder()
                .status(200)
                .message("Get user tickets successfully")
                .data(ticketResponses)
                .build();
        return ResponseEntity.ok(response);
    }
}