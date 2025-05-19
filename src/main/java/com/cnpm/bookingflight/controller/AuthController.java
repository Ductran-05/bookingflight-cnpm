package com.cnpm.bookingflight.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.cnpm.bookingflight.Utils.SecurityUtil;
import com.cnpm.bookingflight.dto.request.LoginDTO;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.ResLoginDTO;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@RestController
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthController {
    final AuthenticationManagerBuilder authenticationManagerBuilder;
    final SecurityUtil securityUtil;

    @PostMapping("/login")
    public ResponseEntity<APIResponse<ResLoginDTO>> login(@Valid @RequestBody LoginDTO loginDTO) {
        // Nạp input gồm username/password vào Security
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginDTO.getUsername(), loginDTO.getPassword());

        // xác thực người dùng => cần viết hàm loadUserByUsername(hàm này sẽ được
        // gọi trong câu lệnh dưới)
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        // tạo token cho người dùng
        String token = securityUtil.generateToken(authentication);

        APIResponse<ResLoginDTO> resLoginDTO = APIResponse.<ResLoginDTO>builder()
                .data(ResLoginDTO
                        .builder()
                        .accessToken(token)
                        .build())
                .build();
        return ResponseEntity.ok(resLoginDTO);
    }
}
