package com.cnpm.bookingflight.mapper;

import com.cnpm.bookingflight.domain.Parameters;
import com.cnpm.bookingflight.dto.request.ParametersRequest;
import org.springframework.stereotype.Component;

@Component
public class ParametersMapper {
    public void updateParametersFromRequest(ParametersRequest request, Parameters parameters) {
        if (request.getMaxInterQuantity() != null) {
            parameters.setMaxInterQuantity(request.getMaxInterQuantity());
        }
        if (request.getMinInterQuantity() != null) {
            parameters.setMinInterQuantity(request.getMinInterQuantity());
        }
        if (request.getMinFlightTime() != null) {
            parameters.setMinFlightTime(request.getMinFlightTime());
        }
        if (request.getMinStopTime() != null) {
            parameters.setMinStopTime(request.getMinStopTime());
        }
        if (request.getMaxStopTime() != null) {
            parameters.setMaxStopTime(request.getMaxStopTime());
        }
        if (request.getLatestBookingDay() != null) {
            parameters.setLatestBookingDay(request.getLatestBookingDay());
        }
        if (request.getLatestCancelDay() != null) {
            parameters.setLatestCancelDay(request.getLatestCancelDay());
        }
        if (request.getRefundRate() != null) {
            parameters.setRefundRate(request.getRefundRate());
        }
    }
}