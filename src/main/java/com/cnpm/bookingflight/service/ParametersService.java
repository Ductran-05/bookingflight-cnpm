package com.cnpm.bookingflight.service;

import com.cnpm.bookingflight.domain.Parameters;
import com.cnpm.bookingflight.dto.request.ParametersRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.ParametersMapper;
import com.cnpm.bookingflight.repository.ParametersRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParametersService {

    final ParametersRepository parametersRepository;
    final ParametersMapper parametersMapper;

    public ResponseEntity<APIResponse<Parameters>> getParameters() {
        Parameters parameters = parametersRepository.findById(1L)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        APIResponse<Parameters> response = APIResponse.<Parameters>builder()
                .status(200)
                .message("Get parameters successfully")
                .data(parameters)
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Parameters>> updateParameters(ParametersRequest request) {
        Parameters existingParameters = parametersRepository.findById(1L)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        parametersMapper.updateParametersFromRequest(request, existingParameters);
        Parameters updatedParameters = parametersRepository.save(existingParameters);

        APIResponse<Parameters> response = APIResponse.<Parameters>builder()
                .status(200)
                .message("Update parameters successfully")
                .data(updatedParameters)
                .build();
        return ResponseEntity.ok(response);
    }
}