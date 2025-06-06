package com.cnpm.bookingflight.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.cnpm.bookingflight.domain.Account;
import com.cnpm.bookingflight.domain.Role;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {

    Optional<Account> findByUsername(String username);

    Optional<Account> findByUsernameAndRefreshToken(String username, String refreshToken);

    List<Account> findAllByIsDeletedFalse();

    Optional<Account> findByUsernameAndIsDeletedFalse(String username);

    boolean existsByRole(Role role);
    boolean existsByRoleId(Long roleId);

}