package com.cnpm.bookingflight.mapper;

import com.cnpm.bookingflight.domain.Parameters;
import com.cnpm.bookingflight.dto.request.ParametersRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ParametersMapper {
    void updateParametersFromRequest(ParametersRequest request, @MappingTarget Parameters parameters);
}