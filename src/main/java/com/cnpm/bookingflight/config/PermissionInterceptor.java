package com.cnpm.bookingflight.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.AntPathMatcher;
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

        String requestURI = request.getRequestURI();
        String httpMethod = request.getMethod();

        System.out.println(">>> RUN preHandle");
        System.out.println(">>> httpMethod = " + httpMethod);
        System.out.println(">>> requestURI = " + requestURI);

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

            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return true;
    }

    private String normalizePath(String path) {
        // Thay {id} hoặc tương tự thành **
        path = path.replaceAll("\\{[^/]+}", "**");

        // Nếu không kết thúc bằng /** và không chứa wildcard thì thêm /** để gom nhóm
        if (!path.endsWith("/**") && !path.contains("*")) {
            path = path.replaceAll("/$", ""); // xóa dấu / cuối nếu có
            path += "/**";
        }

        return path;
    }
}
