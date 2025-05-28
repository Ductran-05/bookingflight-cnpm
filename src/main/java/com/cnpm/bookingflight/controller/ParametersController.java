package com.cnpm.bookingflight.controller;

import com.cnpm.bookingflight.domain.Parameters;
import com.cnpm.bookingflight.dto.request.ParametersRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.service.ParametersService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/parameters")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParametersController {

    final ParametersService parametersService;

    @PutMapping
    public ResponseEntity<APIResponse<Parameters>> updateParameters(
            @Valid @RequestBody ParametersRequest request) {
        return parametersService.updateParameters(request);
    }
}