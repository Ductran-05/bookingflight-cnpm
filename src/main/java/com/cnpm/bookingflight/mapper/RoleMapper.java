package com.cnpm.bookingflight.mapper;

import org.springframework.stereotype.Component;

import com.cnpm.bookingflight.domain.Role;
import com.cnpm.bookingflight.dto.request.RoleRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Component
public class RoleMapper {

    public Role toRole(RoleRequest request) {
        return Role.builder()
                .roleName(request.getRoleName())
                .build();
    }
}
