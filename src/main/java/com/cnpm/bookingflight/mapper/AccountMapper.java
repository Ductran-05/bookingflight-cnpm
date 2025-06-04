package com.cnpm.bookingflight.mapper;

import com.cnpm.bookingflight.domain.Account;
import com.cnpm.bookingflight.domain.Role;
import com.cnpm.bookingflight.dto.request.AccountRequest;
import com.cnpm.bookingflight.dto.response.AccountResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Component
public class AccountMapper {
    final RoleRepository roleRepository;
    final RoleMapper roleMapper;

    public Account toAccount(AccountRequest request) {
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        return Account.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .avatar(request.getAvatar())
                .role(role)
                .build();
    }

    public Account updateAccount(AccountRequest request, Account existingAccount) {
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        existingAccount.setFullName(request.getFullName());
        existingAccount.setPhone(request.getPhone());
        existingAccount.setAvatar(request.getAvatar());
        existingAccount.setRole(role);
        return existingAccount;
    }

    public AccountResponse toAccountResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .username(account.getUsername())
                .fullName(account.getFullName())
                .phone(account.getPhone())
                .avatar(account.getAvatar())
                .role(roleMapper.toRoleResponse(account.getRole()))
                .build();
    }
}