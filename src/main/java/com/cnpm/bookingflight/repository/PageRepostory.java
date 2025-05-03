package com.cnpm.bookingflight.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cnpm.bookingflight.domain.Page;

@Repository
public interface PageRepostory extends JpaRepository<Page, Long> {

}
