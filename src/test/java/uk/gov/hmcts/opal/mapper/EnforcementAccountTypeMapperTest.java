package uk.gov.hmcts.opal.mapper;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.entity.LowHighValue;
import uk.gov.hmcts.opal.entity.enforcement.AccountType;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementAccountType;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementAccountTypeEntity;
import uk.gov.hmcts.opal.generated.model.EnforcementAccountTypeCommon;

public class EnforcementAccountTypeMapperTest {

    private final EnforcementAccountTypeMapper mapper = Mappers.getMapper(EnforcementAccountTypeMapper.class);

    @Test
    void toDto_allFields() {
        var entity = EnforcementAccountTypeEntity.builder()
            .enforcementAccountTypeId(150L)
            .enforcementAccountType(EnforcementAccountType.COMPANY_HIGH)
            .accountType(AccountType.COMPANY)
            .accountTypePath(LowHighValue.HIGH)
            .minimumBalance(BigDecimal.valueOf(2.50))
            .versionNumber(2L)
            .build();

        EnforcementAccountTypeCommon dto = mapper.toDto(entity);

        assertAll(
            () -> assertEquals(150, dto.getId()),
            () -> assertEquals(
                EnforcementAccountTypeCommon.EnforcementAccountTypeEnum.COH, dto.getEnforcementAccountType()
            ),
            () -> assertEquals(EnforcementAccountTypeCommon.AccountTypeEnum.CO, dto.getAccountType()),
            () -> assertEquals(EnforcementAccountTypeCommon.PathEnum.H, dto.getPath()),
            () -> assertEquals(BigDecimal.valueOf(2.50), dto.getMinimumBalance()),
            () -> assertEquals(2, dto.getVersion())
        );
    }

    @Test
    void toDto_allMandatoryFields() {
        var entity = EnforcementAccountTypeEntity.builder()
            .enforcementAccountTypeId(1L)
            .enforcementAccountType(EnforcementAccountType.ADULT_NO_COLLECTION_ORDER_HIGH)
            .accountType(AccountType.ADULT_NO_COLLECTION_ORDER)
            .accountTypePath(LowHighValue.LOW)
            .versionNumber(3L)
            .build();

        EnforcementAccountTypeCommon dto = mapper.toDto(entity);

        assertAll(
            () -> assertEquals(1, dto.getId()),
            () -> assertEquals(
                EnforcementAccountTypeCommon.EnforcementAccountTypeEnum.AH, dto.getEnforcementAccountType()
            ),
            () -> assertEquals(EnforcementAccountTypeCommon.AccountTypeEnum.A, dto.getAccountType()),
            () -> assertEquals(EnforcementAccountTypeCommon.PathEnum.L, dto.getPath()),
            () -> assertNull(dto.getMinimumBalance()),
            () -> assertEquals(3, dto.getVersion())
        );
    }

}
