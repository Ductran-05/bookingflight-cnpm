package com.cnpm.bookingflight.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cnpm.bookingflight.domain.Account;
import com.cnpm.bookingflight.domain.VerificationToken;

import jakarta.transaction.Transactional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);

    void deleteByAccount(Account existingAccount);

    @Transactional
    void deleteByAccountId(Long id);
}