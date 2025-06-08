package com.cnpm.bookingflight.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

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

        String requestURI = request.getRequestURI();
        String httpMethod = request.getMethod();
        String normalizedPath = normalizePath(requestURI);

        // Danh sách public GET
        if (httpMethod.equals("GET")) {
            if (PublicEndpoints.GET_METHODS.contains(normalizedPath)) {
                return true;
            }
        }

        String username = SecurityUtil.getCurrentUserLogin().orElse("");
        if (!username.isEmpty()) {
            Account account = accountRepository.findByUsername(username).orElse(null);

            if (account == null || account.getRole() == null) {
                throw new AppException(ErrorCode.ROLE_NOT_FOUND);
            }

            if ("ADMIN".equals(account.getRole().getRoleName())) {
                return true;
            }

            Role role = account.getRole();
            List<Page> pages = page_RoleRepository.findAllByRole(role).stream()
                    .map(page_Role -> page_Role.getPage())
                    .toList();

            AntPathMatcher matcher = new AntPathMatcher();
            String normalizedRequestURI = normalizePath(requestURI);

            for (Page page : pages) {
                String normalizedApiPath = normalizePath(page.getApiPath());
                if (page.getMethod().equalsIgnoreCase(httpMethod) &&
                        matcher.match(normalizedApiPath, normalizedRequestURI)) {
                    return true;
                }
            }

            throw new AppException(ErrorCode.FORBIDDEN);
        }

        return true;
    }

    private String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "/**";
        }

        String[] segments = path.split("/");
        StringBuilder normalized = new StringBuilder();

        for (String segment : segments) {
            if (segment.isEmpty())
                continue;

            if (isUUID(segment) || isNumeric(segment)) {
                normalized.append("/**");
                break; // Dừng tại đoạn động
            } else {
                normalized.append("/").append(segment);
            }
        }

        if (!normalized.toString().contains("**")) {
            normalized.append("/**");
        }

        return normalized.toString();
    }

    private boolean isUUID(String str) {
        return str
                .matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$");
    }

    private boolean isNumeric(String str) {
        return str.matches("^\\d+$");
    }

}
