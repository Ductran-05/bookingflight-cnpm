package com.cnpm.bookingflight.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cnpm.bookingflight.domain.Account;
import com.cnpm.bookingflight.dto.request.AccountRequest;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "id", ignore = true)
    public Account toAccount(AccountRequest request);

}
