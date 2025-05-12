package com.cnpm.bookingflight.service;

import com.cnpm.bookingflight.domain.Account;
import com.cnpm.bookingflight.dto.request.AccountRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.AccountMapper;
import com.cnpm.bookingflight.repository.AccountRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountService {
    final AccountRepository accountRepository;
    final AccountMapper accountMapper;
    final ImageUploadService imageUploadService;

    public ResponseEntity<APIResponse<List<Account>>> getAllAccounts() {
        APIResponse<List<Account>> response = APIResponse.<List<Account>>builder()
                .status(200)
                .message("Get all accounts successfully")
                .data(accountRepository.findAll())
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Account>> getAccountById(Long id) {
        APIResponse<Account> response = APIResponse.<Account>builder()
                .status(200)
                .message("Get account by id successfully")
                .data(accountRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND)))
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Account>> createAccount(AccountRequest request, MultipartFile avatar) throws IOException {
        Account account = accountRepository.findByUsername(request.getUsername());
        if (account != null) {
            throw new AppException(ErrorCode.EXISTED);
        }

        Account newAccount = accountMapper.toAccount(request);
        if (avatar != null && !avatar.isEmpty()) {
            String avatarUrl = imageUploadService.uploadImage(avatar, "avatars");
            newAccount.setAvatar(avatarUrl);
        }

        APIResponse<Account> response = APIResponse.<Account>builder()
                .status(201)
                .message("Create account successfully")
                .data(accountRepository.save(newAccount))
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Account>> updateAccount(Long id, AccountRequest request, MultipartFile avatar) throws IOException {
        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        Account updatedAccount = accountMapper.toAccount(request);
        updatedAccount.setId(id);
        if (avatar != null && !avatar.isEmpty()) {
            String avatarUrl = imageUploadService.uploadImage(avatar, "avatars");
            updatedAccount.setAvatar(avatarUrl);
        } else {
            updatedAccount.setAvatar(existingAccount.getAvatar());
        }

        APIResponse<Account> response = APIResponse.<Account>builder()
                .status(200)
                .message("Update account successfully")
                .data(accountRepository.save(updatedAccount))
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Void>> deleteAccount(Long id) {
        accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        accountRepository.deleteById(id);
        APIResponse<Void> response = APIResponse.<Void>builder()
                .status(204)
                .message("Delete account successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}
