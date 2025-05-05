package com.cnpm.bookingflight.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cnpm.bookingflight.domain.Page_Role;
import com.cnpm.bookingflight.domain.Role;
import com.cnpm.bookingflight.dto.request.Page_RoleRequest;
import com.cnpm.bookingflight.dto.request.RoleRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.RoleResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.Page_RoleMapper;
import com.cnpm.bookingflight.mapper.RoleMapper;
import com.cnpm.bookingflight.repository.Page_RoleRepository;
import com.cnpm.bookingflight.repository.RoleRepository;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleService {

        final Page_RoleRepository page_RoleRepository;
        final Page_RoleMapper page_RoleMapper;
        final RoleRepository roleRepository;
        final RoleMapper roleMapper;

        public ResponseEntity<APIResponse<List<RoleResponse>>> getRoles() {
                APIResponse<List<RoleResponse>> response = APIResponse.<List<RoleResponse>>builder()
                                .data(roleRepository.findAll().stream().map(roleMapper::toRoleResponse).toList())
                                .status(200)
                                .message("get roles successfully")
                                .build();

                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<RoleResponse>> getRoleById(Long id) {
                APIResponse<RoleResponse> response = APIResponse.<RoleResponse>builder()
                                .data(roleRepository.findById(id)
                                                .map(roleMapper::toRoleResponse)
                                                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND)))
                                .status(200)
                                .message("get role by id successfully")
                                .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<RoleResponse>> createRole(RoleRequest request) {
                Role existingRole = roleRepository.findByRoleName(request.getRoleName());
                if (existingRole != null) {
                        throw new AppException(ErrorCode.EXISTED);
                }
                Role savedRole = roleRepository.save(roleMapper.toRole(request));

                for (Page_RoleRequest pageRoleRequest : request.getPageRoles()) {
                        Page_Role page_Role = page_RoleMapper.toPage_Role(pageRoleRequest,
                                        savedRole);
                        page_RoleRepository.save(page_Role);
                }

                APIResponse<RoleResponse> response = APIResponse.<RoleResponse>builder()
                                .data(roleMapper.toRoleResponse(savedRole))
                                .status(200)
                                .message("create role successfully")
                                .build();

                return ResponseEntity.ok(response);
        }

        @Transactional
        public ResponseEntity<APIResponse<RoleResponse>> updateRole(Long id, RoleRequest request) {
                Role role = roleRepository.findById(id)
                                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

                page_RoleRepository.deleteAllByRole(role);
                role.setRoleName(request.getRoleName());
                roleRepository.save(role);

                for (Page_RoleRequest pageRoleRequest : request.getPageRoles()) {
                        Page_Role page_Role = page_RoleMapper.toPage_Role(pageRoleRequest, role);
                        page_RoleRepository.save(page_Role);
                }

                APIResponse<RoleResponse> response = APIResponse.<RoleResponse>builder()
                                .data(roleMapper.toRoleResponse(role))
                                .status(200)
                                .message("Update role successfully")
                                .build();

                return ResponseEntity.ok(response);

        }

        @Transactional
        public ResponseEntity<APIResponse<Void>> deleteRole(Long id) {
                Role roleToDelete = roleRepository.findById(id)
                                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

                page_RoleRepository.deleteAllByRole(roleToDelete);
                roleRepository.deleteById(id);

                APIResponse<Void> response = APIResponse.<Void>builder()
                                .status(204)
                                .message("Delete role successfully")
                                .build();
                return ResponseEntity.ok(response);
        }
}
/////// cần fix lại trả về
