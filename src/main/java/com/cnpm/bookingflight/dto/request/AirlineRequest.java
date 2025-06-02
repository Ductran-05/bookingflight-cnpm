package com.cnpm.bookingflight.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AirlineRequest {
    @NotBlank(message = "airlineCode must not be blank")
    String airlineCode;

    @NotBlank(message = "airlineName must not be blank")
    String airlineName;

    String logo;
}