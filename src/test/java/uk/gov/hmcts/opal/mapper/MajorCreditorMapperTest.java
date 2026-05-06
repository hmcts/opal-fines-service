package uk.gov.hmcts.opal.mapper;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.dto.reference.MajorCreditorReferenceData;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.entity.majorcreditor.MajorCreditorEntity;

class MajorCreditorMapperTest {

    private final MajorCreditorMapper mapper = Mappers.getMapper(MajorCreditorMapper.class);

    @Test
    @DisplayName("toRefData maps major creditor and creditor account fields")
    void toRefData_mapsMajorCreditorFields() {
        LocalDateTime lastChangedDate = LocalDateTime.of(2026, 4, 23, 9, 30);
        MajorCreditorEntity entity = MajorCreditorEntity.builder()
            .majorCreditorId(101L)
            .businessUnitId((short) 78)
            .majorCreditorCode("MC01")
            .name("Graph Major Creditor")
            .postcode("MC1 1AA")
            .creditorAccountEntity(CreditorAccountEntity.builder()
                .creditorAccountId(202L)
                .accountNumber("AC123456")
                .creditorAccountType(CreditorAccountType.MJ)
                .prosecutionService(true)
                .minorCreditorPartyId(303L)
                .fromSuspense(false)
                .holdPayout(true)
                .lastChangedDate(lastChangedDate)
                .build())
            .build();

        MajorCreditorReferenceData mapped = mapper.toRefData(entity);

        assertAll(
            () -> assertNotNull(mapped),
            () -> assertEquals(101L, mapped.getMajorCreditorId()),
            () -> assertEquals((short) 78, mapped.getBusinessUnitId()),
            () -> assertEquals("MC01", mapped.getMajorCreditorCode()),
            () -> assertEquals("Graph Major Creditor", mapped.getName()),
            () -> assertEquals("MC1 1AA", mapped.getPostcode()),
            () -> assertEquals(202L, mapped.getCreditorAccountId()),
            () -> assertEquals("AC123456", mapped.getAccountNumber()),
            () -> assertEquals("MJ", mapped.getCreditorAccountType()),
            () -> assertEquals(Boolean.TRUE, mapped.getProsecutionService()),
            () -> assertEquals(303L, mapped.getMinorCreditorPartyId()),
            () -> assertEquals(Boolean.FALSE, mapped.getFromSuspense()),
            () -> assertEquals(Boolean.TRUE, mapped.getHoldPayout()),
            () -> assertEquals(lastChangedDate, mapped.getLastChangedDate())
        );
    }
}
