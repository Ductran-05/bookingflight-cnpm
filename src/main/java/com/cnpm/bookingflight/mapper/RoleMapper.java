package com.cnpm.bookingflight.mapper;

import com.cnpm.bookingflight.domain.Page;
import com.cnpm.bookingflight.domain.Role;
import com.cnpm.bookingflight.dto.request.RoleRequest;
import com.cnpm.bookingflight.dto.response.RoleResponse;
import com.cnpm.bookingflight.repository.AccountRepository;
import com.cnpm.bookingflight.repository.Page_RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Component
public class RoleMapper {
    final Page_RoleRepository page_RoleRepository;
    final AccountRepository accountRepository;

    public Role toRole(RoleRequest request) {
        return Role.builder()
                .roleName(request.getRoleName())
                .roleDescription(request.getRoleDescription())
                .build();
    }

    public RoleResponse toRoleResponse(Role role) {
        List<Page> pages = page_RoleRepository.findAllByRole(role).stream()
                .map(page_Role -> page_Role.getPage())
                .toList();
        boolean hasForeignKey = accountRepository.existsByRoleId(role.getId());
        return RoleResponse.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .roleDescription(role.getRoleDescription())
                .pages(pages)
                .canDelete(!hasForeignKey)
                .build();
    }
}