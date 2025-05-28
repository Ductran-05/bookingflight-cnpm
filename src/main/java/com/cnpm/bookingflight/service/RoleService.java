package com.cnpm.bookingflight.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cnpm.bookingflight.domain.Page;
import com.cnpm.bookingflight.domain.Page_Role;
import com.cnpm.bookingflight.domain.Role;
import com.cnpm.bookingflight.dto.ResultPaginationDTO;
import com.cnpm.bookingflight.dto.request.RoleRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.RoleResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.Page_RoleMapper;
import com.cnpm.bookingflight.mapper.ResultPaginationMapper;
import com.cnpm.bookingflight.mapper.RoleMapper;
import com.cnpm.bookingflight.repository.PageRepository;
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
        final PageRepository pageRepository;
        final ResultPaginationMapper resultPaginationMapper;

        public ResponseEntity<APIResponse<ResultPaginationDTO>> getAllRoles(Specification<Role> spec,
                        Pageable pageable) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("isDeleted"), false));

                ResultPaginationDTO result = resultPaginationMapper
                                .toResultPagination(roleRepository.findAll(spec, pageable));
                APIResponse<ResultPaginationDTO> response = APIResponse.<ResultPaginationDTO>builder()
                                .status(200)
                                .message("Get all roles successfully")
                                .data(result)
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
                Role existingRole = roleRepository.findByRoleName(request.getRoleName()).orElse(null);
                if (existingRole != null) {
                        throw new AppException(ErrorCode.EXISTED);
                }
                Role newRole = roleMapper.toRole(request);
                newRole.setIsDeleted(false); // Đảm bảo isDeleted là false khi tạo mới
                Role savedRole = roleRepository.save(newRole);

                for (Long pageId : request.getPages()) {
                        Page savedPage = pageRepository.findById(pageId)
                                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
                        Page_Role page_Role = page_RoleMapper.toPage_Role(savedPage, savedRole);
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
                Role savedRole = roleRepository.findById(id)
                                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

                page_RoleRepository.deleteAllByRole(savedRole);
                savedRole.setRoleName(request.getRoleName());
                savedRole.setIsDeleted(savedRole.getIsDeleted()); // Giữ nguyên trạng thái isDeleted
                roleRepository.save(savedRole);

                for (Long pageId : request.getPages()) {
                        Page savedPage = pageRepository.findById(pageId)
                                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
                        Page_Role page_Role = page_RoleMapper.toPage_Role(savedPage, savedRole);
                        page_RoleRepository.save(page_Role);
                }
                APIResponse<RoleResponse> response = APIResponse.<RoleResponse>builder()
                                .data(roleMapper.toRoleResponse(savedRole))
                                .status(200)
                                .message("Update role successfully")
                                .build();
                return ResponseEntity.ok(response);
        }

        @Transactional
        public ResponseEntity<APIResponse<Void>> deleteRole(Long id) {
                Role roleToDelete = roleRepository.findById(id)
                                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
                roleToDelete.setIsDeleted(true); // Chuyển sang trạng thái xóa mềm
                roleRepository.save(roleToDelete);
                APIResponse<Void> response = APIResponse.<Void>builder()
                                .status(200)
                                .message("Delete role successfully")
                                .build();
                return ResponseEntity.ok(response);
        }
}