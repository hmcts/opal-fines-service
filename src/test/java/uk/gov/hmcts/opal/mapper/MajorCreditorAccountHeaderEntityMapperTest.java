package uk.gov.hmcts.opal.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.entity.majorcreditor.MajorCreditorAccountHeaderEntity;

class MajorCreditorAccountHeaderEntityMapperTest {

    private final MajorCreditorAccountHeaderEntityMapper mapper = new MajorCreditorAccountHeaderEntityMapper();

    @Test
    void toResponse_mapsBusinessUnitCode() {
        MajorCreditorAccountHeaderEntity entity = MajorCreditorAccountHeaderEntity.builder()
            .creditorAccountId(101L)
            .creditorAccountNumber("12345678")
            .creditorAccountType(CreditorAccountType.MJ)
            .versionNumber(8L)
            .businessUnitId((short) 77)
            .businessUnitName("Camberwell Green")
            .businessUnitCode("CBG")
            .name("Major Creditor Ltd")
            .awaitingPayout(new BigDecimal("12.34"))
            .build();

        GetMajorCreditorAccountHeaderSummaryResponse response = mapper.toResponse(entity);

        assertNotNull(response);
        assertEquals(BigInteger.valueOf(8L), response.getVersion());
        assertEquals("77", response.getBusinessUnitDetails().getBusinessUnitId());
        assertEquals("Camberwell Green", response.getBusinessUnitDetails().getBusinessUnitName());
        assertEquals("CBG", response.getBusinessUnitDetails().getBusinessUnitCode());
        assertEquals("N", response.getBusinessUnitDetails().getWelshSpeaking());
        assertEquals(new BigDecimal("12.34"), response.getAwaitingPayout());
    }
}
