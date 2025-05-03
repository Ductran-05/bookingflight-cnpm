package com.cnpm.bookingflight.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cnpm.bookingflight.domain.City;
import com.cnpm.bookingflight.dto.request.CityRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.CityMapper;
import com.cnpm.bookingflight.repository.CityRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CityService {
    final CityRepository cityRepository;
    final CityMapper cityMapper;

    public ResponseEntity<APIResponse<List<City>>> getAllCities() {
        APIResponse<List<City>> response = APIResponse.<List<City>>builder()
                .status(200)
                .message("Get all cities successfully")
                .data(cityRepository.findAll())
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<City>> getCityById(Long id) {
        APIResponse<City> response = APIResponse.<City>builder()
                .status(200)
                .message("Get city by id successfully")
                .data(cityRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND)))
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<City>> createCity(CityRequest request) {
        City existingCity = cityRepository.findByCityCode(request.getCityCode());
        if (existingCity != null) {
            throw new AppException(ErrorCode.EXISTED);
        }

        APIResponse<City> response = APIResponse.<City>builder()
                .status(201)
                .message("Create city successfully")
                .data(cityRepository.save(cityMapper.toCity(request)))
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<City>> updateCity(Long id, CityRequest request) {
        cityRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        City existingCity = cityMapper.toCity(request);
        existingCity.setId(id);
        cityRepository.save(existingCity);
        APIResponse<City> response = APIResponse.<City>builder()
                .status(200)
                .message("Update city successfully")
                .data(existingCity)
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Void>> deleteCity(Long id) {
        cityRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        cityRepository.deleteById(id);
        APIResponse<Void> response = APIResponse.<Void>builder()
                .status(204)
                .message("Delete city successfully")
                .build();
        return ResponseEntity.ok(response);
    }

}
