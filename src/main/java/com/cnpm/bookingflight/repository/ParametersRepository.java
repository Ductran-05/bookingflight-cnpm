package com.cnpm.bookingflight.repository;

import com.cnpm.bookingflight.domain.Parameters;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParametersRepository extends JpaRepository<Parameters, Long> {
}