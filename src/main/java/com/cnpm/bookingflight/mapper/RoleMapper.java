package com.cnpm.bookingflight.mapper;

import org.springframework.stereotype.Component;

import com.cnpm.bookingflight.domain.Role;
import com.cnpm.bookingflight.dto.request.RoleRequest;
import com.cnpm.bookingflight.dto.response.RoleResponse;
import com.cnpm.bookingflight.repository.Page_RoleRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Component
public class RoleMapper {
    final Page_RoleRepository pageRoleRepository;

    public Role toRole(RoleRequest request) {
        return Role.builder()
                .roleName(request.getRoleName())
                .build();
    }

    public RoleResponse toRoleResponse(Role role) {
        System.out.println(pageRoleRepository.findAllByRole(role));
        RoleResponse response = RoleResponse.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .pageRoles(pageRoleRepository.findAllByRole(role))
                .build();
        return response;
    }
}
