package com.cnpm.bookingflight.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    // xu ly exception chua biet
    @ExceptionHandler(value = Exception.class)
    ResponseEntity<APIResponse<Void>> handleAppException(Exception e) {
        APIResponse<Void> response = APIResponse.<Void>builder()
                .status(ErrorCode.UNIDENTIFIED_EXCEPTION.getCode())
                // .message(ErrorCode.UNIDENTIFIED_EXCEPTION.getMessage())
                .message(e.getMessage())
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    // xử lý các trường hợp khong thỏa mãn ràng buộc
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<APIResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        APIResponse<Void> response = APIResponse.<Void>builder()
                .status(ErrorCode.INVALID.getCode())
                .message(e.getMessage())
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<APIResponse<String>> handleBindException(BindException e) {
        // Trả về message của lỗi đầu tiên
        String enumName = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        ErrorCode errorCode = ErrorCode.valueOf(enumName);

        APIResponse<String> response = APIResponse.<String>builder()
                .status(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    // xu ly truong hop thong tin nguoi dung khong hop le
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
