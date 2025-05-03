package com.cnpm.bookingflight.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountService {
    final AccountRepository accountRepository;
    final AccountMapper accountMapper;

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

    public ResponseEntity<APIResponse<Account>> createAccount(AccountRequest request) {
        Account account = accountRepository.findByUsername(request.getUsername());
        if (account != null) {
            throw new AppException(ErrorCode.EXISTED);
        }

        APIResponse<Account> response = APIResponse.<Account>builder()
                .status(200)
                .message("Create account successfully")
                .data(accountRepository.save(accountMapper.toAccount(request)))
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Account>> updateAccount(Long id, AccountRequest request) {
        accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        Account existingAccount = accountMapper.toAccount(request);
        existingAccount.setId(id);
        accountRepository.save(existingAccount);
        APIResponse<Account> response = APIResponse.<Account>builder()
                .status(200)
                .message("Update account successfully")
                .data(existingAccount)
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
