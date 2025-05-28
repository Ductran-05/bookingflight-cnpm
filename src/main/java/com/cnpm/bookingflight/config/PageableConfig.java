package com.cnpm.bookingflight.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class PageableConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();

        resolver.setFallbackPageable(PageRequest.of(0, Integer.MAX_VALUE)); // Không giới hạn mặc định
        resolver.setMaxPageSize(Integer.MAX_VALUE); // Không giới hạn tối đa
        resolver.setOneIndexedParameters(true); // page bắt đầu từ 1
        resolvers.add(resolver);
    }
}
