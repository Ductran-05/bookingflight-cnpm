package com.cnpm.bookingflight.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cnpm.bookingflight.dto.response.APIResponse;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalException {

    // Xử lý exception thông thường
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<APIResponse<Void>> handleAppException(AppException exception) {
        APIResponse<Void> response = APIResponse.<Void>builder()
                .status(exception.getErrorCode().getCode())
                .message(exception.getErrorCode().getMessage())
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    // Xử lý exception chưa biết
    @ExceptionHandler(value = Exception.class)
    ResponseEntity<APIResponse<Void>> handleAppException(Exception e) {
        APIResponse<Void> response = APIResponse.<Void>builder()
                .status(ErrorCode.UNIDENTIFIED_EXCEPTION.getCode())
                .message(e.getMessage())
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    // Xử lý các trường hợp không thỏa mãn ràng buộc
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<APIResponse<Map<String, String>>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        APIResponse<Map<String, String>> response = APIResponse.<Map<String, String>>builder()
                .status(ErrorCode.INVALID.getCode())
                .message("Validation failed")
                .data(errors)
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<APIResponse<String>> handleBindException(BindException e) {
        String enumName = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        ErrorCode errorCode = ErrorCode.valueOf(enumName);

        APIResponse<String> response = APIResponse.<String>builder()
                .status(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    // Xử lý trường hợp thông tin người dùng không hợp lệ
    @ExceptionHandler(value = {
            UsernameNotFoundException.class,
            BadCredentialsException.class })
    ResponseEntity<APIResponse<Void>> handleException(Exception e) {
        APIResponse<Void> response = APIResponse.<Void>builder()
                .status(ErrorCode.INVALID_INPUT.getCode())
                .message(e.getMessage())
                .build();
        return ResponseEntity.badRequest().body(response);
    }
}