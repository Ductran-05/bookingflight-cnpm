package com.cnpm.bookingflight.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.cnpm.bookingflight.domain.Page;

@Repository
public interface PageRepository extends JpaRepository<Page, Long>, JpaSpecificationExecutor<Page> {

    Optional<Page> findByPageName(String pageName);

}
