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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;

@Component
public class AppInitializer {

    private final RequestMappingHandlerMapping handlerMapping;
    private final PageRepository pageRepository;
    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final Page_RoleRepository page_RoleRepository;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    public AppInitializer(
            @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping,
            PageRepository pageRepository,
            RoleRepository roleRepository,
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            Page_RoleRepository page_RoleRepository) {
        this.handlerMapping = handlerMapping;
        this.pageRepository = pageRepository;
        this.roleRepository = roleRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.page_RoleRepository = page_RoleRepository;
    }

    @PostConstruct
    public void init() {
        initializePages();
        initializeAdmin();
        initializeUser();
    }

    private void initializePages() {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();

        for (var entry : handlerMethods.entrySet()) {
            RequestMappingInfo mappingInfo = entry.getKey();
            Set<String> urlPatterns = resolvePatterns(mappingInfo);
            Set<RequestMethod> methods = mappingInfo.getMethodsCondition().getMethods();

            for (String rawUrl : urlPatterns) {
                for (RequestMethod method : methods) {
                    String methodName = method.name();

                    // Bỏ qua các endpoint public không cần tạo quyền
                    if (PublicEndpoints.isPublic(rawUrl, HttpMethod.valueOf(methodName)))
                        continue;
                    String normalizedUrl = normalizePath(rawUrl);

                    if (pageRepository.existsByApiPathAndMethod(normalizedUrl, methodName))
                        continue;

                    Page page = Page.builder()
                            .apiPath(normalizedUrl)
                            .method(methodName)
                            .name(generatePermissionName(extractModule(normalizedUrl), methodName))
                            .module(extractModule(normalizedUrl))
                            .build();

                    pageRepository.save(page);
                }
            }
        }
    }

    // Phần còn lại giữ nguyên như bạn đã viết

    private void initializeAdmin() {
        Role adminRole = roleRepository.findByRoleName("ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().roleName("ADMIN").build()));

        List<Page> allPages = pageRepository.findAll();
        for (Page page : allPages) {
            if (page_RoleRepository.existsByPageAndRole(page, adminRole))
                continue;
            Page_Role page_Role = Page_Role.builder()
                    // set id từ page và role
                    .id(new Page_RoleId(page.getId(), adminRole.getId()))
                    .page(page)
                    .role(adminRole)
                    .build();
            page_RoleRepository.save(page_Role);
        }

        boolean existsAdmin = accountRepository.existsByRole(adminRole);
        if (!existsAdmin) {
            Account adminAccount = Account.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin"))
                    .fullName("Admin")
                    .role(adminRole)
                    .enabled(true)
                    .build();
            accountRepository.save(adminAccount);
        }
    }

    private void initializeUser() {
        Role userRole = roleRepository.findByRoleName("USER")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .roleName("USER")
                        .build()));

        for (Page page : pageRepository.findAll()) {
            if (antPathMatcher.match("/my-account/**", page.getApiPath())
                    || antPathMatcher.match("/booking-flight/**", page.getApiPath())) {
                if (page_RoleRepository.existsByPageAndRole(page, userRole))
                    continue;
                Page_Role page_Role = Page_Role.builder()
                        .id(new Page_RoleId(page.getId(), userRole.getId()))
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
        path = path.replaceAll("\\{[^/]+}", "**");

        if (!path.endsWith("/**")) {
            path = path.replaceAll("/$", "");
            path += "/**";
        }

        return path;
    }

    private String generatePermissionName(String model, String method) {
        return method + "_" + model;
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
