package com.cnpm.bookingflight.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)

public class Flight_SeatRequest {
    Long seatId;
    Integer quantity;
}
