package com.cnpm.bookingflight.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cnpm.bookingflight.Utils.SecurityUtil;
import com.cnpm.bookingflight.dto.request.AccountRequest;
import com.cnpm.bookingflight.dto.request.LoginDTO;
import com.cnpm.bookingflight.dto.request.RegisterRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.AccountResponse;
import com.cnpm.bookingflight.dto.response.ResLoginDTO;

import com.cnpm.bookingflight.mapper.AccountMapper;
import com.cnpm.bookingflight.repository.AccountRepository;
import com.cnpm.bookingflight.service.AccountService;

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
        final AccountRepository accountRepository;
        final AccountMapper accountMapper;
        final AccountService accountService;
        final PasswordEncoder passwordEncoder;

        @PostMapping("/login")
        public ResponseEntity<APIResponse<ResLoginDTO>> login(@Valid @RequestBody LoginDTO loginDTO) {
                // Nạp input gồm username/password vào Security
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                loginDTO.getUsername(), loginDTO.getPassword());

                // xác thực người dùng => cần viết hàm loadUserByUsername(hàm này sẽ được
                // gọi trong câu lệnh dưới)
                Authentication authentication = authenticationManagerBuilder.getObject()
                                .authenticate(authenticationToken);
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

        @PostMapping(value = "register", consumes = { "multipart/form-data" })
        public ResponseEntity<APIResponse<AccountResponse>> createAccount(
                        @RequestPart("registerInfo") RegisterRequest request,
                        @RequestPart(value = "avatar", required = false) MultipartFile avatar) throws IOException {

                String hashPassword = passwordEncoder.encode(request.getPassword());
                request.setPassword(hashPassword);
                AccountRequest accountRequest = AccountRequest.builder()
                                .username(request.getUsername())
                                .password(request.getPassword())
                                .fullName(request.getFullName())
                                .phone(request.getPhone())
                                .avatar(request.getAvatar())
                                .roleId(null)
                                .build();

                return accountService.createAccount(accountRequest, avatar);
        }

}
