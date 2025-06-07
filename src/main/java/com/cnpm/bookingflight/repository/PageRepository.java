package com.cnpm.bookingflight.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.cnpm.bookingflight.domain.Page;

@Repository
public interface PageRepository extends JpaRepository<Page, Long>, JpaSpecificationExecutor<Page> {

    Optional<Page> findByName(String name);

    boolean existsByApiPathAndMethod(String pattern, String name);

    List<Page> findByMethodAndApiPath(String method, String apiPath);

}
