package com.cnpm.bookingflight.config;

import java.util.List;

import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;

public class PublicEndpoints {

        private static final AntPathMatcher pathMatcher = new AntPathMatcher();

        public static final List<String> ALL_METHODS = List.of(
                        "/auth/**",
                        "/dashboard/**",
                        "/pages/**",
                        "/my-profile/**");

        public static final List<String> GET_METHODS = List.of(
                        "/airports/**",
                        "/permissions/**",
                        "/cities/**",
                        "/airlines/**",
                        "/flights/**",
                        "/seats/**",
                        "/planes/**",
                        "/parameters/**");

        /* 
        
        */

        public static boolean isPublic(String path, HttpMethod method) {
                // Check ALL_METHODS first
                for (String publicPath : ALL_METHODS) {
                        if (pathMatcher.match(publicPath, path)) {
                                return true;
                        }
                }

                // Check GET_METHODS for GET requests
                if (HttpMethod.GET.equals(method)) {
                        for (String publicPath : GET_METHODS) {
                                if (pathMatcher.match(publicPath, path)) {
                                        return true;
                                }
                        }
                }

                return false;
        }
}
