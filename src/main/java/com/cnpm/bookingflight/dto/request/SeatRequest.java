package com.cnpm.bookingflight.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatRequest {
    @NotBlank(message = "seatCode must not be blank")
    String seatCode;

    @NotBlank(message = "seatName must not be blank")
    String seatName;

    @NotNull(message = "price must not be null")
    @Min(value = 0, message = "price must be non-negative")
    Integer price;

    String description;
}