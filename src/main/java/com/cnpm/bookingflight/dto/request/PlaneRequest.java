package com.cnpm.bookingflight.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlaneRequest {
    @NotBlank(message = "planeCode must not be blank")
    String planeCode;

    @NotBlank(message = "planeName must not be blank")
    String planeName;

    @NotNull(message = "airlineId must not be null")
    Long airlineId;
}