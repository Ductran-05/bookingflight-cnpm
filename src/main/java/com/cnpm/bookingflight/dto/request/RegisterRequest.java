package com.cnpm.bookingflight.dto.request;

import lombok.Data;

@Data
public class RegisterRequest {

    private String username;

    private String password;

    private String email;

    private String fullName;

    private String phone; // không bắt buộc

    private String avatar; // không bắt buộc
}