package com.cnpm.bookingflight.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.cnpm.bookingflight.domain.Page;
import com.cnpm.bookingflight.domain.Role;
import com.cnpm.bookingflight.dto.request.RoleRequest;
import com.cnpm.bookingflight.dto.response.RoleResponse;
// import com.cnpm.bookingflight.repository.PageRepository;
import com.cnpm.bookingflight.repository.Page_RoleRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Component
public class RoleMapper {
    // final PageRepository pageRepository;
    final Page_RoleRepository page_RoleRepository;

    public Role toRole(RoleRequest request) {
        return Role.builder()
                .roleName(request.getRoleName())
                .build();
    }

    public RoleResponse toRoleResponse(Role role) {

        List<Page> pages = page_RoleRepository.findAllByRole(role).stream().map(page_Role -> page_Role.getPage())
                .toList();
        RoleResponse response = RoleResponse.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .pages(pages)
                .build();
        return response;
    }

}
