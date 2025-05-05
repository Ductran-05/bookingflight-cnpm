package com.cnpm.bookingflight.mapper;

import org.springframework.stereotype.Component;

import com.cnpm.bookingflight.domain.Page;
import com.cnpm.bookingflight.dto.request.PageRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class PageMapper {

    public Page toPage(PageRequest pageRequest) {
        return Page.builder()
                .pageName(pageRequest.getPageName())
                .build();
    }
}
