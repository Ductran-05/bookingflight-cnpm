package com.cnpm.bookingflight.dto.response;

import com.cnpm.bookingflight.domain.Page;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleResponse {
    Long id;
    String roleName;
    String roleDescription;
    List<Page> pages;
    Boolean canDelete;
}