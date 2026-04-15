package uk.gov.hmcts.opal.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.hmcts.opal.dto.DraftAccountResponseDto;
import uk.gov.hmcts.opal.dto.DraftAccountSummaryDto;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.entity.draft.DraftAccountType;

@SpringJUnitConfig
@ContextConfiguration(classes = {
    DraftAccountMapperImpl.class
})
class DraftAccountMapperTest {

    @Autowired
    private DraftAccountMapper mapper;

    @Test
    void givenDraftAccountEntity_whenToResponseDto_thenMapsExpectedFields() {

        //Arrange
        DraftAccountEntity entity = buildEntity();

        //Act
        DraftAccountResponseDto mapped = mapper.toResponseDto(entity);

        //Assert
        assertNotNull(mapped);
        assertEquals(1001L, mapped.getDraftAccountId());
        assertEquals(Short.valueOf((short) 77), mapped.getBusinessUnitId());
        assertEquals(OffsetDateTime.of(2026, 3, 1, 10, 15, 30, 0, ZoneOffset.UTC), mapped.getCreatedDate());
        assertEquals("USER01", mapped.getSubmittedBy());
        assertEquals("Normal User", mapped.getSubmittedByName());
        assertEquals(OffsetDateTime.of(2026, 3, 3, 12, 45, 0, 0, ZoneOffset.UTC), mapped.getValidatedDate());
        assertEquals("VAL01", mapped.getValidatedBy());
        assertEquals("Validator User", mapped.getValidatedByName());
        assertEquals("{\"account\":\"body\"}", mapped.getAccount());
        assertEquals("{\"snapshot\":\"body\"}", mapped.getAccountSnapshot());
        assertEquals(DraftAccountType.FINE, mapped.getAccountType());
        assertEquals(DraftAccountStatus.SUBMITTED, mapped.getAccountStatus());
        assertEquals("status message", mapped.getStatusMessage());
        assertEquals(OffsetDateTime.of(2026, 3, 4, 9, 0, 0, 0, ZoneOffset.UTC), mapped.getAccountStatusDate());
        assertEquals("{\"timeline\":\"body\"}", mapped.getTimelineData());
        assertEquals("ACC-123", mapped.getAccountNumber());
        assertEquals(2002L, mapped.getAccountId());
        assertEquals(BigInteger.valueOf(6L), mapped.getVersion());
    }

    @Test
    void givenDraftAccountEntity_whenToDto_thenMapsExpectedSummaryFields() {

        //Arrange
        DraftAccountEntity entity = buildEntity();

        //Act
        DraftAccountSummaryDto mapped = mapper.toDto(entity);

        //Assert
        assertNotNull(mapped);
        assertEquals(1001L, mapped.getDraftAccountId());
        assertEquals(Short.valueOf((short) 77), mapped.getBusinessUnitId());
        assertEquals(OffsetDateTime.of(2026, 3, 1, 10, 15, 30, 0, ZoneOffset.UTC), mapped.getCreatedDate());
        assertEquals("USER01", mapped.getSubmittedBy());
        assertEquals("Normal User", mapped.getSubmittedByName());
        assertEquals(OffsetDateTime.of(2026, 3, 3, 12, 45, 0, 0, ZoneOffset.UTC), mapped.getValidatedDate());
        assertEquals("VAL01", mapped.getValidatedBy());
        assertEquals("Validator User", mapped.getValidatedByName());
        assertEquals("{\"snapshot\":\"body\"}", mapped.getAccountSnapshot());
        assertEquals(DraftAccountType.FINE, mapped.getAccountType());
        assertEquals(DraftAccountStatus.SUBMITTED, mapped.getAccountStatus());
        assertEquals("ACC-123", mapped.getAccountNumber());
        assertEquals(2002L, mapped.getAccountId());
        assertEquals(LocalDate.of(2026, 3, 4), mapped.getAccountStatusDate());
        assertEquals("status message", mapped.getStatusMessage());
        assertEquals(BigInteger.valueOf(6L), mapped.getVersion());
    }

    @Test
    void givenNullSourceFields_whenMapping_thenNullConversionsArePreserved() {

        //Arrange
        DraftAccountEntity entity = DraftAccountEntity.builder()
            .draftAccountId(1002L)
            .businessUnit(null)
            .createdDate(null)
            .validatedDate(null)
            .accountStatusDate(null)
            .versionNumber(null)
            .build();

        //Act
        final DraftAccountResponseDto response = mapper.toResponseDto(entity);
        final DraftAccountSummaryDto summary = mapper.toDto(entity);

        //Assert
        assertNull(response.getBusinessUnitId());
        assertNull(response.getCreatedDate());
        assertNull(response.getValidatedDate());
        assertNull(response.getAccountStatusDate());
        assertNull(response.getVersion());

        assertNull(summary.getBusinessUnitId());
        assertNull(summary.getCreatedDate());
        assertNull(summary.getValidatedDate());
        assertNull(summary.getAccountStatusDate());
        assertNull(summary.getVersion());
    }

    @Test
    void givenDraftAccountEntity_whenClone_thenCopiesValuesToDistinctInstance() {

        //Arrange
        DraftAccountEntity entity = buildEntity();

        //Act
        DraftAccountEntity cloned = mapper.clone(entity);

        //Assert
        assertNotNull(cloned);
        assertNotSame(entity, cloned);
        assertEquals(entity.getDraftAccountId(), cloned.getDraftAccountId());
        assertEquals(entity.getSubmittedBy(), cloned.getSubmittedBy());
        assertEquals(entity.getSubmittedByName(), cloned.getSubmittedByName());
        assertEquals(entity.getValidatedBy(), cloned.getValidatedBy());
        assertEquals(entity.getValidatedByName(), cloned.getValidatedByName());
        assertEquals(entity.getAccount(), cloned.getAccount());
        assertEquals(entity.getAccountSnapshot(), cloned.getAccountSnapshot());
        assertEquals(entity.getAccountType(), cloned.getAccountType());
        assertEquals(entity.getAccountStatus(), cloned.getAccountStatus());
        assertEquals(entity.getStatusMessage(), cloned.getStatusMessage());
        assertEquals(entity.getTimelineData(), cloned.getTimelineData());
        assertEquals(entity.getAccountNumber(), cloned.getAccountNumber());
        assertEquals(entity.getAccountId(), cloned.getAccountId());
        assertEquals(entity.getVersionNumber(), cloned.getVersionNumber());
        assertSame(entity.getBusinessUnit(), cloned.getBusinessUnit());
    }

    private DraftAccountEntity buildEntity() {
        return DraftAccountEntity.builder()
            .draftAccountId(1001L)
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 77).build())
            .createdDate(LocalDateTime.of(2026, 3, 1, 10, 15, 30))
            .submittedBy("USER01")
            .submittedByName("Normal User")
            .validatedDate(LocalDateTime.of(2026, 3, 3, 12, 45, 0))
            .validatedBy("VAL01")
            .validatedByName("Validator User")
            .account("{\"account\":\"body\"}")
            .accountType(DraftAccountType.FINE)
            .accountId(2002L)
            .accountSnapshot("{\"snapshot\":\"body\"}")
            .accountStatus(DraftAccountStatus.SUBMITTED)
            .accountStatusDate(LocalDateTime.of(2026, 3, 4, 9, 0, 0))
            .statusMessage("status message")
            .timelineData("{\"timeline\":\"body\"}")
            .accountNumber("ACC-123")
            .versionNumber(6L)
            .build();
    }
}
