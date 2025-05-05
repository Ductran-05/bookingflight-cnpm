package com.cnpm.bookingflight.mapper;

import org.springframework.stereotype.Component;

import com.cnpm.bookingflight.domain.Page;
import com.cnpm.bookingflight.domain.Page_Role;
import com.cnpm.bookingflight.domain.Role;
import com.cnpm.bookingflight.domain.id.Page_RoleId;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Component
public class Page_RoleMapper {

    public Page_Role toPage_Role(Page page, Role role) {
        return Page_Role.builder()
                .id(new Page_RoleId(page.getId(), role.getId()))
                .page(page)
                .role(role)
                .build();
    }

}
