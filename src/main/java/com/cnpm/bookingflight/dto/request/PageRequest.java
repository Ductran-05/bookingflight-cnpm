package com.cnpm.bookingflight.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PageRequest {
    @NotBlank(message = "Role name is required")
    String name;
    @NotBlank(message = "Api path is required")
    String apiPath;
    @NotBlank(message = "Method is required")
    String method;
    @NotBlank(message = "Module is required")
    String module;
}
