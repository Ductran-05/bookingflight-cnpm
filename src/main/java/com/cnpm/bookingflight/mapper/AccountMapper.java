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
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();

        textEncryptor.setPassword("mySecretKey");

        String encryptedPassword = textEncryptor.encrypt(request.getPassword());
        return Account.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .avatar(request.getAvatar())
                .username(request.getUsername())
                .password(encryptedPassword)
                .role(roleRepository.findById(request.getRoleId())
                        .orElseThrow(() -> new AppException(ErrorCode.INVALID)))
                .build();
    }

    public AccountResponse toAccountResponse(Account account) {
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();

        textEncryptor.setPassword("mySecretKey");

        String decryptedPassword = textEncryptor.decrypt(account.getPassword());
        return AccountResponse.builder()
                .id(account.getId())
                .username(account.getUsername())
                .password(decryptedPassword)
                .email(account.getEmail())
                .fullName(account.getFullName())
                .phone(account.getPhone())
                .avatar(account.getAvatar())
                .role(account.getRole())
                .build();
    }
}
