package com.cnpm.bookingflight.repository;

import com.cnpm.bookingflight.domain.Airline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.cnpm.bookingflight.domain.Plane;

import java.util.List;

@Repository
public interface PlaneRepository extends JpaRepository<Plane, Long>, JpaSpecificationExecutor<Plane> {
    boolean existsByAirlineId(Long airlineId);
    Plane findByPlaneCode(String planeCode);
    List<Plane> findByAirlineId(Long airlineId);
}
