package com.cnpm.bookingflight.service;

import com.cnpm.bookingflight.domain.City;
import com.cnpm.bookingflight.dto.ResultPaginationDTO;
import com.cnpm.bookingflight.dto.request.CityRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.CityResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.CityMapper;
import com.cnpm.bookingflight.mapper.ResultPaginationMapper;
import com.cnpm.bookingflight.repository.CityRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CityService {
        final CityRepository cityRepository;
        final CityMapper cityMapper;
        final ResultPaginationMapper resultPaginationMapper;

        public ResponseEntity<APIResponse<ResultPaginationDTO>> getAllCities(Specification<City> spec,
                                                                             Pageable pageable) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("isDeleted"), false));
                ResultPaginationDTO result = resultPaginationMapper
                        .toResultPagination(cityRepository.findAll(spec, pageable).map(cityMapper::toCityResponse));
                APIResponse<ResultPaginationDTO> response = APIResponse.<ResultPaginationDTO>builder()
                        .status(200)
                        .message("Get all cities successfully")
                        .data(result)
                        .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<CityResponse>> getCityById(Long id) {
                City city = cityRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
                APIResponse<CityResponse> response = APIResponse.<CityResponse>builder()
                        .status(200)
                        .message("Get city by id successfully")
                        .data(cityMapper.toCityResponse(city))
                        .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<CityResponse>> createCity(CityRequest request) {
                City existingCity = cityRepository.findByCityCode(request.getCityCode());
                if (existingCity != null) {
                        throw new AppException(ErrorCode.EXISTED);
                }
                City newCity = cityMapper.toCity(request);
                newCity.setIsDeleted(false);
                APIResponse<CityResponse> response = APIResponse.<CityResponse>builder()
                        .status(201)
                        .message("Create city successfully")
                        .data(cityMapper.toCityResponse(cityRepository.save(newCity)))
                        .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<CityResponse>> updateCity(Long id, CityRequest request) {
                City existingCity = cityRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
                City updatedCity = cityMapper.toCity(request);
                updatedCity.setId(id);
                updatedCity.setIsDeleted(existingCity.getIsDeleted());
                APIResponse<CityResponse> response = APIResponse.<CityResponse>builder()
                        .status(200)
                        .message("Update city successfully")
                        .data(cityMapper.toCityResponse(cityRepository.save(updatedCity)))
                        .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<Void>> deleteCity(Long id) {
                City existingCity = cityRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
                existingCity.setIsDeleted(true);
                cityRepository.save(existingCity);
                APIResponse<Void> response = APIResponse.<Void>builder()
                        .status(200)
                        .message("Delete city successfully")
                        .build();
                return ResponseEntity.ok(response);
        }
}