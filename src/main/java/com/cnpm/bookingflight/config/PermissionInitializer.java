package com.cnpm.bookingflight.config;

import com.cnpm.bookingflight.domain.Page;
import com.cnpm.bookingflight.repository.PageRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
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
    private static final List<String> WHITE_LIST_PREFIXES = List.of(
            "/auth", "/error");

    @PostConstruct
    public void initializePages() {
        // Lấy toàn bộ endpoint trong ứng dụng (RequestMappingInfo)
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            RequestMappingInfo mappingInfo = entry.getKey();

            // Hỗ trợ Spring Boot 2 và 3
            Set<String> urlPatterns = resolvePatterns(mappingInfo);
            Set<RequestMethod> methods = mappingInfo.getMethodsCondition().getMethods();

            for (String url : urlPatterns) {
                // if (isWhiteListed(url))
                // continue;

                for (RequestMethod method : methods) {
                    // Kiểm tra đã tồn tại page tương ứng chưa
                    boolean exists = pageRepository.existsByApiPathAndMethod(url, method.name());
                    if (!exists) {
                        // Tạo mới bản ghi Page
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

    // Trích xuất tất cả pattern URL từ RequestMappingInfo (Spring Boot 2 & 3)
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

    // Kiểm tra xem URL có thuộc white list không
    private boolean isWhiteListed(String path) {
        return WHITE_LIST_PREFIXES.stream().anyMatch(path::startsWith);
    }

    // Tạo tên hiển thị cho page (ví dụ: "GET /api/v1/flights")
    private String generateName(String path, String method) {
        return method + " " + path;
    }

    // Trích module từ URL path (ví dụ: "/api/v1/flights" -> "Flights")
    private String extractModule(String path) {
        String[] parts = path.split("/");
        for (String part : parts) {
            if (!part.isBlank() && !part.equalsIgnoreCase("api") && !part.matches("v[0-9]+")) {
                return capitalize(part);
            }
        }
        return "General";
    }

    // Viết hoa chữ cái đầu (Flights, Users, Tickets...)
    private String capitalize(String s) {
        if (s == null || s.isEmpty())
            return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
