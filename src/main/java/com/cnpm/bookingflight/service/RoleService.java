package com.cnpm.bookingflight.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cnpm.bookingflight.domain.Role;
import com.cnpm.bookingflight.dto.request.RoleRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.RoleMapper;
import com.cnpm.bookingflight.repository.RoleRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleService {
    final RoleRepository roleRepository;
    final RoleMapper roleMapper;

    public ResponseEntity<APIResponse<List<Role>>> getRoles() {
        APIResponse<List<Role>> response = APIResponse.<List<Role>>builder()
                .data(roleRepository.findAll())
                .status(200)
                .message("get roles successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Role>> getRoleById(Long id) {
        APIResponse<Role> response = APIResponse.<Role>builder()
                .data(roleRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND)))
                .status(200)
                .message("get role by id successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Role>> createRole(RoleRequest request) {
        Role existingRole = roleRepository.findByRoleName(request.getRoleName());
        if (existingRole != null) {
            throw new AppException(ErrorCode.EXISTED);
        }
        APIResponse<Role> response = APIResponse.<Role>builder()
                .data(roleRepository.save(roleMapper.toRole(request)))
                .status(200)
                .message("create role successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Role>> updateRole(Long id, RoleRequest request) {
        roleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        Role existingRole = roleMapper.toRole(request);
        existingRole.setId(id);
        APIResponse<Role> response = APIResponse.<Role>builder()
                .data(roleRepository.save(existingRole))
                .status(200)
                .message("update role successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Void>> deleteRole(Long id) {
        roleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        roleRepository.deleteById(id);
        APIResponse<Void> response = APIResponse.<Void>builder()
                .status(204)
                .message("Delete role successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}
