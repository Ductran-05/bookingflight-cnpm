package com.cnpm.bookingflight.domain;

import com.cnpm.bookingflight.domain.id.Page_RoleId;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Page_Role {
    @EmbeddedId
    Page_RoleId id;

    @ManyToOne
    @MapsId("pageId")
    @JoinColumn(name = "page_id")
    Page page;

    @ManyToOne
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    Role role;
}