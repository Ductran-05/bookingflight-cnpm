package com.cnpm.bookingflight.repository;

import com.cnpm.bookingflight.domain.Page_Role;
import com.cnpm.bookingflight.domain.Role;
import com.cnpm.bookingflight.domain.id.Page_RoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Page_RoleRepository extends JpaRepository<Page_Role, Page_RoleId> {
    List<Page_Role> findAllByRole(Role role);
    void deleteAllByRole(Role role);
    boolean existsByRoleId(Long roleId);
}