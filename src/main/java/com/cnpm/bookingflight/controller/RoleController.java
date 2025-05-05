package com.cnpm.bookingflight.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cnpm.bookingflight.dto.request.RoleRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.RoleResponse;
import com.cnpm.bookingflight.service.RoleService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleController {
    final RoleService roleService;

    @GetMapping()
    public ResponseEntity<APIResponse<List<RoleResponse>>> getAllRoles() {
        return roleService.getRoles();
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<RoleResponse>> getRoleById(@PathVariable("id") Long id) {
        return roleService.getRoleById(id);
    }

    @PostMapping()
    public ResponseEntity<APIResponse<RoleResponse>> createRole(@RequestBody RoleRequest request) {
        return roleService.createRole(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<RoleResponse>> updateRole(@PathVariable("id") Long id,
            @RequestBody RoleRequest request) {
        return roleService.updateRole(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<Void>> deleteRole(@PathVariable("id") Long id) {
        return roleService.deleteRole(id);
    }
}
