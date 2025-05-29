package com.cnpm.bookingflight.config;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.cnpm.bookingflight.domain.Account;
import com.cnpm.bookingflight.domain.Role;
import com.cnpm.bookingflight.repository.AccountRepository;
import com.cnpm.bookingflight.repository.RoleRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer {
    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        if (!roleRepository.existsByRoleName("ADMIN")) {
            Role adminRole = Role.builder()
                    .roleName("ADMIN")
                    .roleDescription("Admin role")
                    .build();
            roleRepository.save(adminRole);
        }
        // tạo account admin
        if (accountRepository.existsByRole(roleRepository.findByRoleName("ADMIN").get())) {
            return;
        }

        Account account = Account.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin"))
                .role(roleRepository.findByRoleName("ADMIN").get())
                .enabled(true)
                .isDeleted(false)
                .build();
        accountRepository.save(account);
    }
}
