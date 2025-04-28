package com.cnpm.bookingflight.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cnpm.bookingflight.domain.Plane;

public interface PlaneRepository extends JpaRepository<Plane, Long> {
    Plane findByPlaneCode(String planeCode);
}
