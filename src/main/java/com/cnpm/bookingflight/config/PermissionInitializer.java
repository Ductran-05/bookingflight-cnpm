package com.cnpm.bookingflight.config;

import com.cnpm.bookingflight.domain.Page;
import com.cnpm.bookingflight.repository.PageRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;

@Component
@RequiredArgsConstructor
public class PermissionInitializer {

    private final RequestMappingHandlerMapping handlerMapping;
    private final PageRepository pageRepository;

    // Danh sách các URL không cần phân quyền (bỏ qua)
    private static final List<String> WHITE_LIST_PATTERNS = List.of(
            "/auth/**", "/error", "/pages/**");

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @PostConstruct
    public void initializePages() {
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
        return path.replaceAll("\\{[^/]+}", "**");
    }

    // Kiểm tra xem URL có thuộc white list không, hỗ trợ ** bằng AntPathMatcher
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
