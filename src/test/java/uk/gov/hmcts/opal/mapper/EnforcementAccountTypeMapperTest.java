package uk.gov.hmcts.opal.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.dto.EnforcementAccountType;
import uk.gov.hmcts.opal.entity.LowHighValue;
import uk.gov.hmcts.opal.entity.enforcement.AccountType;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementAccountTypeEntity;
import uk.gov.hmcts.opal.generated.model.EnforcementAccountTypeCommon;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class EnforcementAccountTypeMapperTest {
    private final EnforcementAccountTypeMapper mapper = Mappers.getMapper(EnforcementAccountTypeMapper.class);

    @Test
    void toDto_allFields() {
        var entity = EnforcementAccountTypeEntity.builder()
            .enforcementAccountTypeId(150L)
            .enforcementAccountType(EnforcementAccountType.COH)
            .accountType(AccountType.CO)
            .accountTypePath(LowHighValue.H)
            .minimumBalance(BigDecimal.valueOf(2.50))
            .versionNumber(2L)
            .build();

        EnforcementAccountTypeCommon dto = mapper.toDto(entity);

        assertAll(
            () -> assertEquals(150, dto.getId()),
            () -> assertEquals(EnforcementAccountTypeCommon.EnforcementAccountTypeEnum.COH, dto.getEnforcementAccountType()),
            () -> assertEquals(EnforcementAccountTypeCommon.AccountTypeEnum.CO, dto.getAccountType()),
            () -> assertEquals(EnforcementAccountTypeCommon.PathEnum.H, dto.getPath()),
            () -> assertEquals(BigDecimal.valueOf(2.50), dto.getMinimumBalance()),
            () -> assertEquals(2, dto.getVersion())
        );
    }

    void toDto_allMandatoryFields() {
        var entity = EnforcementAccountTypeEntity.builder()
            .enforcementAccountTypeId(1L)
            .enforcementAccountType(EnforcementAccountType.AH)
            .accountType(AccountType.A)
            .accountTypePath(LowHighValue.L)
            .versionNumber(3L)
            .build();

        EnforcementAccountTypeCommon dto = mapper.toDto(entity);

        assertAll(
            () -> assertEquals(1, dto.getId()),
            () -> assertEquals(EnforcementAccountTypeCommon.EnforcementAccountTypeEnum.AH, dto.getEnforcementAccountType()),
            () -> assertEquals(EnforcementAccountTypeCommon.AccountTypeEnum.A, dto.getAccountType()),
            () -> assertEquals(EnforcementAccountTypeCommon.PathEnum.L, dto.getPath()),
            () -> assertNull(dto.getMinimumBalance()),
            () -> assertEquals(3, dto.getVersion())
        );
    }

}
