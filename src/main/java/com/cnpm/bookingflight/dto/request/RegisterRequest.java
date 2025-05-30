package com.cnpm.bookingflight.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "username must not be blank")
    @Size(min = 3, max = 50, message = "username must be between 3 and 50 characters")
    String username;

    @NotBlank(message = "password must not be blank")
    @Size(min = 6, max = 100, message = "password must be between 6 and 100 characters")
    String password;

    @NotBlank(message = "fullName must not be blank")
    @Size(max = 100, message = "fullName must not exceed 100 characters")
    String fullName;

    @NotBlank(message = "phone must not be blank")
    @Pattern(regexp = "^0\\d{9,10}$", message = "phone must be a valid phone number: starting with 0 and having 10 to 11 digits")
    String phone;

    String avatar;
}