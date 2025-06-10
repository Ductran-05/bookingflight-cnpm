package com.cnpm.bookingflight.dto.request;

import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleRequest {
    String roleName;
    String roleDescription;

    // List<Page_RoleRequest> pageRoles;
    List<PageInfo> pageInfos;

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @NoArgsConstructor
    public static class PageInfo {
        String method;
        String apiPath;
    }
}
