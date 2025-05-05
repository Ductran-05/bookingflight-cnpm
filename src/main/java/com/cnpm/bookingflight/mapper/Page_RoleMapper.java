package com.cnpm.bookingflight.mapper;

import org.springframework.stereotype.Component;

import com.cnpm.bookingflight.domain.Page_Role;
import com.cnpm.bookingflight.domain.Role;
import com.cnpm.bookingflight.domain.id.Page_RoleId;
import com.cnpm.bookingflight.dto.request.Page_RoleRequest;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.repository.PageRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Component
public class Page_RoleMapper {

    final PageRepository pageRepository;

    public Page_Role toPage_Role(Page_RoleRequest request, Role role) {
        return Page_Role.builder()
                .id(new Page_RoleId(request.getPageId(), role.getId()))
                .page(pageRepository.findById(request.getPageId())
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND)))
                .role(role)
                .build();
    }

}
