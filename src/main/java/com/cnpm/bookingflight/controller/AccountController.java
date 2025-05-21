package com.cnpm.bookingflight.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.cnpm.bookingflight.dto.request.AccountRequest;
import com.cnpm.bookingflight.dto.request.ChangePasswordRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.AccountResponse;
import com.cnpm.bookingflight.service.AccountService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/accounts")
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AccountController {

    final AccountService accountService;
    final PasswordEncoder passwordEncoder;

    @GetMapping()
    public ResponseEntity<APIResponse<List<AccountResponse>>> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<AccountResponse>> getAccountById(@PathVariable("id") Long id) {
        return accountService.getAccountById(id);
    }

    @PostMapping(consumes = { "multipart/form-data" })
    public ResponseEntity<APIResponse<AccountResponse>> createAccount(@RequestPart("account") AccountRequest request,
                                                                      @RequestPart(value = "avatar", required = false) MultipartFile avatar) throws IOException {
        String hashPassword = passwordEncoder.encode(request.getPassword());
        request.setPassword(hashPassword);
        return accountService.createAccount(request, avatar);
    }

    @PutMapping(value = "/{id}", consumes = { "multipart/form-data" })
    public ResponseEntity<APIResponse<AccountResponse>> updateAccount(@PathVariable("id") Long id,
                                                                      @RequestPart("account") AccountRequest request,
                                                                      @RequestPart(value = "avatar", required = false) MultipartFile avatar) throws IOException {
        return accountService.updateAccount(id, request, avatar);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<Void>> deleteAccount(@PathVariable("id") Long id) {
        return accountService.deleteAccount(id);
    }

    @PostMapping("/{id}/change-password")
    public ResponseEntity<APIResponse<Void>> changePassword(@PathVariable("id") Long id,
                                                            @RequestBody ChangePasswordRequest request) {
        return accountService.changePassword(id, request);
    }
}