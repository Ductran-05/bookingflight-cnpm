package com.cnpm.bookingflight.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cnpm.bookingflight.dto.response.APIResponse;

@RestControllerAdvice
public class GlobalException {
    // xu ly exception thong thuong
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<APIResponse<Void>> handleAppException(AppException exception) {
        APIResponse<Void> response = APIResponse.<Void>builder()
                .status(exception.getErrorCode().getCode())
                .message(exception.getErrorCode().getMessage())
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<APIResponse<Void>> handleAppException(Exception e) {
        APIResponse<Void> response = APIResponse.<Void>builder()
                .status(ErrorCode.UNIDENTIFIED_EXCEPTION.getCode())
                // .message(ErrorCode.UNIDENTIFIED_EXCEPTION.getMessage())
                .message(e.getMessage())
                .build();
        return ResponseEntity.badRequest().body(response);
    }

}