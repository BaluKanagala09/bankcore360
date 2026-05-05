package org.cts.transactionservice.mapper;

import org.cts.transactionservice.dto.response.AccountDto;
import org.cts.transactionservice.dto.response.AccountResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting AccountResponse from Account Service
 * to AccountDto used in Transaction Service.
 *
 * AccountResponse comes from the Account Service REST API,
 * while AccountDto is the DTO used internally in Transaction Service.
 */
@Mapper(componentModel = "spring")
public interface AccountMapper {

    /**
     * Convert AccountResponse to AccountDto with field mapping.
     *
     * Field mappings:
     * - id (Long) → accountId (Long)
     * - accountStatus (String) → accountStatus (String)
     * - balance (BigDecimal) → balance (BigDecimal)
     * - customerId (Long) → customerId (Long)
     * - branchId (Long) → branchId (Long)
     *
     * @param accountResponse the source object from Account Service
     * @return AccountDto for use in Transaction Service
     */
    @Mapping(source = "id", target = "accountId")
    AccountDto toAccountDto(AccountResponse accountResponse);
}


