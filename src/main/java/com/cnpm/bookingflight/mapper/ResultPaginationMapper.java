package com.cnpm.bookingflight.mapper;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.cnpm.bookingflight.dto.Pagination;
import com.cnpm.bookingflight.dto.ResultPaginationDTO;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Component
@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResultPaginationMapper {

    public ResultPaginationDTO toResultPagination(Page<?> page) {
        return ResultPaginationDTO.builder()
                .pagination(Pagination.builder()
                        .page(page.getNumber() + 1)
                        .pageSize(page.getSize())
                        .pages(page.getTotalPages())
                        .total(page.getTotalElements())
                        .build())
                .result(page.getContent())
                .build();
    }
}
