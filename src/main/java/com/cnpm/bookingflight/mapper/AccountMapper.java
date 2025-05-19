package com.cnpm.bookingflight.mapper;

import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.stereotype.Component;

import com.cnpm.bookingflight.domain.Account;
import com.cnpm.bookingflight.dto.request.AccountRequest;
import com.cnpm.bookingflight.dto.response.AccountResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.repository.RoleRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Component
public class AccountMapper {
    final RoleRepository roleRepository;

    public Account toAccount(AccountRequest request) {

        return Account.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .avatar(request.getAvatar())
                .username(request.getUsername())
                .password(request.getPassword())
                .role(roleRepository.findById(request.getRoleId())
                        .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND)))
                .build();
    }

    public AccountResponse toAccountResponse(Account account) {

        return AccountResponse.builder()
                .id(account.getId())
                .username(account.getUsername())
                .password(account.getPassword())
                .email(account.getEmail())
                .fullName(account.getFullName())
                .phone(account.getPhone())
                .avatar(account.getAvatar())
                .role(account.getRole())
                .build();
    }
}
