package com.cnpm.bookingflight.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CityRequest {
    @NotBlank(message = "cityCode must not be blank")
    String cityCode;

    @NotBlank(message = "cityName must not be blank")
    String cityName;
}