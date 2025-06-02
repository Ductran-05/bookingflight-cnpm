package com.cnpm.bookingflight.config;

import com.cnpm.bookingflight.domain.Account;
import com.cnpm.bookingflight.domain.Page;
import com.cnpm.bookingflight.domain.Page_Role;
import com.cnpm.bookingflight.domain.Role;
import com.cnpm.bookingflight.domain.id.Page_RoleId;
import com.cnpm.bookingflight.repository.AccountRepository;
import com.cnpm.bookingflight.repository.PageRepository;
import com.cnpm.bookingflight.repository.Page_RoleRepository;
import com.cnpm.bookingflight.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;

@Component
@RequiredArgsConstructor
public class AppInitializer {

    private final RequestMappingHandlerMapping handlerMapping;
    private final PageRepository pageRepository;
    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final Page_RoleRepository page_RoleRepository;

    private static final List<String> WHITE_LIST_PATTERNS = List.of(
            "/auth/**", "/error", "/pages/**");

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @PostConstruct
    public void init() {
        initializePages();
        initializeAdmin();
        initializeUser();

    }

    private void initializePages() {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            RequestMappingInfo mappingInfo = entry.getKey();

            Set<String> urlPatterns = resolvePatterns(mappingInfo);
            Set<RequestMethod> methods = mappingInfo.getMethodsCondition().getMethods();

            for (String rawUrl : urlPatterns) {
                String url = normalizePath(rawUrl);
                if (isWhiteListed(url))
                    continue;
                for (RequestMethod method : methods) {
                    boolean exists = pageRepository.existsByApiPathAndMethod(url, method.name());
                    if (!exists) {
                        Page page = Page.builder()
                                .apiPath(url)
                                .method(method.name())
                                .name(generateName(url, method.name()))
                                .module(extractModule(url))
                                .build();
                        pageRepository.save(page);
                    }
                }
            }
        }
    }

    private void initializeAdmin() {
        Role adminRole = roleRepository.findByRoleName("ADMIN").orElseGet(() -> {
            Role newRole = Role.builder()
                    .roleName("ADMIN")
                    .roleDescription("Admin role")
                    .build();
            return roleRepository.save(newRole);
        });
        // gán pages cho role admin
        for (Page page : pageRepository.findAll()) {
            Page_Role page_Role = Page_Role.builder()
                    .id(new Page_RoleId(page.getId(), adminRole.getId()))
                    .page(page)
                    .role(adminRole)
                    .build();

            page_RoleRepository.save(page_Role);
        }

        boolean existsAdmin = accountRepository.existsByRole(adminRole);
        if (!existsAdmin) {
            Account account = Account.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin"))
                    .role(adminRole)
                    .enabled(true)
                    .isDeleted(false)
                    .build();
            accountRepository.save(account);
        }

    }

    private void initializeUser() {
        Role userRole = roleRepository.findByRoleName("USER").orElseGet(() -> {
            Role newRole = Role.builder()
                    .roleName("USER")
                    .roleDescription("User role")
                    .build();
            return roleRepository.save(newRole);
        });

        // Lấy danh sách các API cho phép user access (ví dụ GET các API public)
        List<String> allowedPaths = List.of("/flights/**", "/tickets/**");

        List<Page> allowedPagesForUser = pageRepository.findAll().stream()
                .filter(page -> page.getMethod().equalsIgnoreCase("GET") &&
                        allowedPaths.stream().anyMatch(pattern -> pathMatcher.match(pattern, page.getApiPath())))
                .toList();

        for (Page page : allowedPagesForUser) {
            Page_RoleId id = new Page_RoleId(page.getId(), userRole.getId());
            if (!page_RoleRepository.existsById(id)) {
                Page_Role page_Role = Page_Role.builder()
                        .id(id)
                        .page(page)
                        .role(userRole)
                        .build();
                page_RoleRepository.save(page_Role);
            }
        }
    }

    private Set<String> resolvePatterns(RequestMappingInfo info) {
        Set<String> patterns = new HashSet<>();
        if (info.getPatternsCondition() != null) {
            patterns.addAll(info.getPatternsCondition().getPatterns());
        } else if (info.getPathPatternsCondition() != null) {
            info.getPathPatternsCondition().getPatterns()
                    .forEach(p -> patterns.add(p.getPatternString()));
        }
        return patterns;
    }

    private String normalizePath(String path) {
        // Thay {id}, {abc}... bằng **
        path = path.replaceAll("\\{[^/]+}", "**");

        // Nếu path không kết thúc bằng '/**' và không có phần mở rộng sau tiền tố (ví
        // dụ: /accounts), thêm '/**'
        if (!path.endsWith("/**")) {
            path = path.replaceAll("/$", ""); // xóa dấu "/" cuối nếu có
            path += "/**";
        }

        return path;
    }

    private boolean isWhiteListed(String path) {
        return WHITE_LIST_PATTERNS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private String generateName(String path, String method) {
        return method + " " + path;
    }

    private String extractModule(String path) {
        String[] parts = path.split("/");
        for (String part : parts) {
            if (!part.isBlank() && !part.equalsIgnoreCase("api") && !part.matches("v[0-9]+")) {
                return capitalize(part);
            }
        }
        return "General";
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty())
            return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
