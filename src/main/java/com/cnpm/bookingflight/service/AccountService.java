package com.cnpm.bookingflight.service;

import com.cnpm.bookingflight.domain.Account;
import com.cnpm.bookingflight.domain.VerificationToken;
import com.cnpm.bookingflight.dto.request.AccountRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.AccountResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.AccountMapper;
import com.cnpm.bookingflight.repository.AccountRepository;
import com.cnpm.bookingflight.repository.VerificationTokenRepository;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountService {

    private final EmailService emailService;

    private final VerificationTokenRepository verificationTokenRepository;

    final PasswordEncoder passwordEncoder;
    final AccountRepository accountRepository;
    final AccountMapper accountMapper;
    final ImageUploadService imageUploadService;

    public ResponseEntity<APIResponse<List<AccountResponse>>> getAllAccounts() {
        APIResponse<List<AccountResponse>> response = APIResponse.<List<AccountResponse>>builder()
                .status(200)
                .message("Get all accounts successfully")
                .data(accountRepository.findAll().stream().map(accountMapper::toAccountResponse).toList())
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<AccountResponse>> getAccountById(Long id) {
        APIResponse<AccountResponse> response = APIResponse.<AccountResponse>builder()
                .status(200)
                .message("Get account by id successfully")
                .data(accountMapper.toAccountResponse(
                        accountRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND))))
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<AccountResponse>> createAccount(AccountRequest request, MultipartFile avatar)
            throws IOException {
        Account account = accountRepository.findByUsername(request.getUsername());
        if (account != null) {
            throw new AppException(ErrorCode.EXISTED);
        }

        Account newAccount = accountMapper.toAccount(request);
        if (avatar != null && !avatar.isEmpty()) {
            String avatarUrl = imageUploadService.uploadImage(avatar, "avatars");
            newAccount.setAvatar(avatarUrl);
        }

        APIResponse<AccountResponse> response = APIResponse.<AccountResponse>builder()
                .status(201)
                .message("Create account successfully")
                .data(accountMapper.toAccountResponse(accountRepository.save(newAccount)))
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<AccountResponse>> updateAccount(Long id, AccountRequest request,
            MultipartFile avatar)
            throws IOException {
        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        Account updatedAccount = accountMapper.toAccount(request);
        updatedAccount.setId(id);
        updatedAccount.setPassword(existingAccount.getPassword()); // Giữ nguyên password
        if (avatar != null && !avatar.isEmpty()) {
            String avatarUrl = imageUploadService.uploadImage(avatar, "avatars");
            updatedAccount.setAvatar(avatarUrl);
        } else {
            updatedAccount.setAvatar(existingAccount.getAvatar());
        }

        APIResponse<AccountResponse> response = APIResponse.<AccountResponse>builder()
                .status(200)
                .message("Update account successfully")
                .data(accountMapper.toAccountResponse(accountRepository.save(updatedAccount)))
                .build();
        return ResponseEntity.ok(response);
    }

    @Transactional
    public ResponseEntity<APIResponse<Void>> deleteAccount(Long id) {
        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        verificationTokenRepository.deleteByAccount(existingAccount);
        accountRepository.deleteById(id);
        APIResponse<Void> response = APIResponse.<Void>builder()
                .status(204)
                .message("Delete account successfully")
                .build();
        return ResponseEntity.ok(response);
    }

}
