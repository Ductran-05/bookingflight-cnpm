package com.cnpm.bookingflight.service;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.cnpm.bookingflight.domain.Account;
import com.cnpm.bookingflight.repository.AccountRepository;

import lombok.Data;

@Component("userDetailsService")
@Data
public class UserDetailsCustom implements UserDetailsService {
    private final AccountRepository accountRepository;
    private final AccountService accountService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsername(username).orElse(null);
        if (account == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        return new User(
                account.getUsername(),
                account.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("USER")));
    }

}
