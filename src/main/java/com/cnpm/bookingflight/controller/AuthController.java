package com.cnpm.bookingflight.controller;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cnpm.bookingflight.Utils.SecurityUtil;
import com.cnpm.bookingflight.domain.Account;
import com.cnpm.bookingflight.domain.VerificationToken;
import com.cnpm.bookingflight.dto.request.LoginDTO;
import com.cnpm.bookingflight.dto.request.RegisterRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.ResLoginDTO;

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
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthController {
        final AuthenticationManagerBuilder authenticationManagerBuilder;
        final SecurityUtil securityUtil;
        final AccountRepository accountRepository;
        final AccountMapper accountMapper;
        final AccountService accountService;
        final PasswordEncoder passwordEncoder;
        final VerificationTokenRepository verificationTokenRepository;

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

        @PostMapping("/register")
        public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
                accountService.registerUser(request);
                return ResponseEntity.ok("Đăng ký thành công! Vui lòng kiểm tra email để xác thực.");
        }

        @GetMapping("/auth/confirm")
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
