package com.cnpm.bookingflight.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
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
import com.cnpm.bookingflight.dto.response.LoginResponse.UserLogin;
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
                SecurityContextHolder.getContext().setAuthentication(authentication);
                Account currAccount = accountRepository.findByUsername(loginDTO.getUsername());

                // khởi tạo ResponseLogin
                UserLogin userLogin = UserLogin.builder()
                                .id(currAccount.getId())
                                .name(currAccount.getFullName())
                                .username(currAccount.getUsername())
                                .build();
                LoginResponse loginResponse = LoginResponse.builder()
                                .accessToken(securityUtil.createAccessToken(loginDTO.getUsername(), userLogin))
                                .user(userLogin)
                                .build();
                // tao refresh token
                String refreshToken = securityUtil.createRefreshToken(loginDTO.getUsername(), loginResponse);
                // update refresh token cho account
                accountService.updateAccountRefreshToken(refreshToken, loginDTO.getUsername());
                // set cookie refresh token
                ResponseCookie responseCookie = ResponseCookie.from("refreshToken", refreshToken)
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(securityConfig.getRefreshTokenExpiration())
                                .build();

                // response data
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
        public ResponseEntity<APIResponse<UserLogin>> getUserLogin() {
                String username = SecurityUtil.getCurrentUserLogin().isPresent()
                                ? SecurityUtil.getCurrentUserLogin().get()
                                : "";
                Account currAccount = accountRepository.findByUsername(username);

                LoginResponse.UserLogin userLogin = UserLogin.builder()
                                .id(currAccount.getId())
                                .name(currAccount.getFullName())
                                .username(currAccount.getUsername())
                                .build();

                APIResponse<UserLogin> response = APIResponse.<UserLogin>builder()
                                .status(200)
                                .message("Get user login successfully")
                                .data(userLogin)
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
                LoginResponse.UserLogin userLogin = UserLogin.builder()
                                .id(currAccount.getId())
                                .name(currAccount.getFullName())
                                .username(currAccount.getUsername())
                                .build();
                LoginResponse loginResponse = LoginResponse.builder()
                                .accessToken(securityUtil.createAccessToken(username, userLogin))
                                .user(userLogin)
                                .build();
                // tao refresh token moi
                String newRefreshToken = securityUtil.createRefreshToken(username, loginResponse);
                // updateAccount
                accountService.updateAccountRefreshToken(newRefreshToken, username);
                // set new cookie
                ResponseCookie responseCookie = ResponseCookie.from("refreshToken", newRefreshToken)
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(securityConfig.getRefreshTokenExpiration())
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

}
