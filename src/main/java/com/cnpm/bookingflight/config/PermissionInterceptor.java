package com.cnpm.bookingflight.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import com.cnpm.bookingflight.Utils.SecurityUtil;
import com.cnpm.bookingflight.domain.Account;
import com.cnpm.bookingflight.domain.Page;
import com.cnpm.bookingflight.domain.Role;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.repository.AccountRepository;
import com.cnpm.bookingflight.repository.Page_RoleRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;

public class PermissionInterceptor implements HandlerInterceptor {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private Page_RoleRepository page_RoleRepository;

    @Override
    @Transactional
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response, Object handler)
            throws Exception {

        String path = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String requestURI = request.getRequestURI();
        String httpMethod = request.getMethod();
        System.out.println(">>> RUN preHandle");
        System.out.println(">>> path= " + path);
        System.out.println(">>> httpMethod= " + httpMethod);
        System.out.println(">>> requestURI= " + requestURI);
        // check permission
        String username = SecurityUtil.getCurrentUserLogin().orElse("");
        if (!username.isEmpty()) {
            Account account = accountRepository.findByUsername(username).orElse(null);
            if (account.getRole().getRoleName().equals("ADMIN")) {
                return true;
            }
            if (account != null) {
                Role role = account.getRole();
                if (role != null) {
                    List<Page> pages = page_RoleRepository.findAllByRole(role).stream()
                            .map(page_Role -> page_Role.getPage())
                            .toList();
                    if (pages != null) {
                        for (Page page : pages) {
                            if (page.getApiPath().equals(path) && page.getMethod().equals(httpMethod)) {
                                return true;
                            }
                        }
                        throw new AppException(ErrorCode.UNAUTHORIZED);
                    }
                }
            }
        }
        return true;
    }
}
