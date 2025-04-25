package com.cnpm.bookingflight.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cnpm.bookingflight.domain.City;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {

    City findByCityCode(String cityCode);

}
