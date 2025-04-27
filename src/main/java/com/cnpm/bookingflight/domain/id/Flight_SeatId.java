package com.cnpm.bookingflight.domain.id;

import java.io.Serializable;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Embeddable
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class Flight_SeatId implements Serializable {
    Long flightId;
    Long seatId;
}
