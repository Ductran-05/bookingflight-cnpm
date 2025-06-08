package com.cnpm.bookingflight.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import com.cnpm.bookingflight.dto.request.UpdateProfileRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.cnpm.bookingflight.Utils.SecurityUtil;
import com.cnpm.bookingflight.config.SecurityConfig;
import com.cnpm.bookingflight.domain.Account;
import com.cnpm.bookingflight.domain.VerificationToken;
import com.cnpm.bookingflight.dto.request.LoginDTO;
import com.cnpm.bookingflight.dto.request.RegisterRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.AccountResponse;
import com.cnpm.bookingflight.dto.response.LoginResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.AccountMapper;
import com.cnpm.bookingflight.repository.AccountRepository;
import com.cnpm.bookingflight.repository.VerificationTokenRepository;
import com.cnpm.bookingflight.service.AccountService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@RestController
@Data
@RequestMapping("/auth")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthController {
        final AuthenticationManagerBuilder authenticationManagerBuilder;
        final SecurityUtil securityUtil;
        final AccountRepository accountRepository;
        final AccountMapper accountMapper;
        final AccountService accountService;
        final PasswordEncoder passwordEncoder;
        final VerificationTokenRepository verificationTokenRepository;
        final SecurityConfig securityConfig;

        @PostMapping("/login")
        public ResponseEntity<APIResponse<LoginResponse>> login(@Valid @RequestBody LoginDTO loginDTO) {
                // Nạp input gồm username/password vào Security
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                loginDTO.getUsername(), loginDTO.getPassword());
                // xác thực người dùng => cần viết hàm loadUserByUsername
                Authentication authentication = authenticationManagerBuilder.getObject()
                                .authenticate(authenticationToken);
                Account currAccount = accountRepository.findByUsername(loginDTO.getUsername())
                                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
                if (!currAccount.getEnabled())
                        throw new AppException(ErrorCode.ACCOUNT_INACTIVE);
                // set data cho contextholder
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // tao access token
                AccountResponse currAccountResponse = accountMapper.toAccountResponse(currAccount);
                String accessToken = securityUtil.createAccessToken(loginDTO.getUsername(), currAccountResponse);
                // tao refresh token
                String refreshToken = securityUtil.createRefreshToken(loginDTO.getUsername(), currAccountResponse);
                // update refresh token cho account
                accountService.updateAccountRefreshToken(refreshToken, loginDTO.getUsername());
                // set cookie refresh token
                ResponseCookie responseCookie = ResponseCookie.from("refreshToken", refreshToken)
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(securityConfig.getRefreshTokenExpiration())
                                .build();
                LoginResponse loginResponse = LoginResponse.builder()
                                .accessToken(accessToken)
                                .account(currAccountResponse)
                                .build();
                APIResponse<LoginResponse> response = APIResponse.<LoginResponse>builder()
                                .status(200)
                                .message("Login successfully")
                                .data(loginResponse)
                                .build();
                return ResponseEntity.ok()
                                .header("Set-Cookie", responseCookie.toString())
                                .body(response);
        }

        // lay nguoi dung da dang nhap
        @GetMapping("/user")
        public ResponseEntity<APIResponse<AccountResponse>> getUserLogin() {
                String username = SecurityUtil.getCurrentUserLogin().isPresent()
                                ? SecurityUtil.getCurrentUserLogin().get()
                                : "";
                Account currAccount = accountRepository.findByUsername(username)
                                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

                AccountResponse currAccountResponse = accountMapper.toAccountResponse(currAccount);
                APIResponse<AccountResponse> response = APIResponse.<AccountResponse>builder()
                                .status(200)
                                .message("Get user login successfully")
                                .data(currAccountResponse)
                                .build();
                return ResponseEntity.ok(response);
        }

        // lay nguoi dung da dang nhap tu cookie
        @GetMapping("/refresh")
        public ResponseEntity<APIResponse<LoginResponse>> refreshToken(
                        @CookieValue(name = "refreshToken", defaultValue = "") String refreshToken) {
                // check valid refresh token
                Jwt decodedTokenJwt = securityUtil.checkValidRefreshToken(refreshToken);
                String username = decodedTokenJwt.getSubject();
                // kiem tra co ton tai tai khoan dua vao usename va refresh token
                Account currAccount = accountService.findByUsernameAndRefreshToken(username, refreshToken);
                AccountResponse currAccountResponse = accountMapper.toAccountResponse(currAccount);
                // tao access token moi
                String newAccessToken = securityUtil.createAccessToken(username, currAccountResponse);
                // tao refresh token moi
                String newRefreshToken = securityUtil.createRefreshToken(username, currAccountResponse);
                // updateAccount
                accountService.updateAccountRefreshToken(newRefreshToken, username);
                // set new cookie
                ResponseCookie responseCookie = ResponseCookie.from("refreshToken", newRefreshToken)
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(securityConfig.getRefreshTokenExpiration())
                                .build();
                LoginResponse loginResponse = LoginResponse.builder()
                                .accessToken(newAccessToken)
                                .account(currAccountResponse)
                                .build();
                // response data
                APIResponse<LoginResponse> response = APIResponse.<LoginResponse>builder()
                                .status(200)
                                .message("Refresh token successfully")
                                .data(loginResponse)
                                .build();
                return ResponseEntity.ok()
                                .header("Set-Cookie", responseCookie.toString())
                                .body(response);
        }

        @PostMapping("/logout")
        public ResponseEntity<APIResponse<String>> logout() {
                String username = SecurityUtil.getCurrentUserLogin().isPresent()
                                ? SecurityUtil.getCurrentUserLogin().get()
                                : "";

                accountService.updateAccountRefreshToken("", username);
                // set "" cookie refresh token
                ResponseCookie responseCookie = ResponseCookie.from("refreshToken", "")
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(0)
                                .build();

                APIResponse<String> response = APIResponse.<String>builder()
                                .status(200)
                                .message("Logout successfully")
                                .data("")
                                .build();
                return ResponseEntity.ok()
                                .header("Set-Cookie", responseCookie.toString())
                                .body(response);
        }

        @PostMapping(value = "/register", consumes = { "multipart/form-data" })
        public ResponseEntity<APIResponse<AccountResponse>> register(
                        @RequestPart("registerRequest") RegisterRequest request,
                        @RequestPart(value = "avatar", required = false) MultipartFile avatar) throws IOException {
                String hashPassword = passwordEncoder.encode(request.getPassword());
                request.setPassword(hashPassword);
                return accountService.registerUser(request);
        }

        @GetMapping("/confirm")
        public String confirm(@RequestParam("token") String token) {
                Optional<VerificationToken> optionalToken = verificationTokenRepository.findByToken(token);

                if (optionalToken.isEmpty())
                        return "Invalid token";

                VerificationToken verificationToken = optionalToken.get();

                if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                        return "Token expired";
                }

                Account account = verificationToken.getAccount();
                account.setEnabled(true);
                accountRepository.save(account);

                verificationTokenRepository.delete(verificationToken); // Optionally remove token
                return "Email confirmed. Account activated.";
        }

        @PutMapping(value = "/profile", consumes = { "multipart/form-data" })
        public ResponseEntity<APIResponse<AccountResponse>> updateProfile(
                @RequestPart("profile") UpdateProfileRequest request,
                @RequestPart(value = "avatar", required = false) MultipartFile avatar) throws IOException {
                String username = SecurityUtil.getCurrentUserLogin()
                        .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
                return accountService.updateProfile(username, request, avatar);
        }
}
