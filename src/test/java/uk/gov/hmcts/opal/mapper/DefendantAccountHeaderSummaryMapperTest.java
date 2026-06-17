package uk.gov.hmcts.opal.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import tools.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountHeaderViewEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountStatus;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountType;

class DefendantAccountHeaderSummaryMapperTest {

    private final DefendantAccountHeaderSummaryMapper mapper =
        Mappers.getMapper(DefendantAccountHeaderSummaryMapper.class);

    @Test
    void mapToDto_setsParentGuardianDebtorTypeAndYouthFlag() {
        DefendantAccountHeaderViewEntity entity = DefendantAccountHeaderViewEntity.builder()
            .debtorType(null)
            .hasParentGuardian(true)
            .birthDate(LocalDate.now().minusYears(15))
            .accountStatus(DefendantAccountStatus.LIVE)
            .version(1L)
            .build();

        DefendantAccountHeaderSummary dto = mapper.toDto(entity);
        assertEquals("Parent/Guardian", dto.getDebtorType());
        assertTrue(dto.getIsYouth());
    }

    @Test
    void mapToDto_setsDefaultDebtorTypeAndNotYouthWhenBirthDateMissing() {
        DefendantAccountHeaderViewEntity entity = DefendantAccountHeaderViewEntity.builder()
            .debtorType(null)
            .hasParentGuardian(false)
            .birthDate(null)
            .accountStatus(DefendantAccountStatus.LIVE)
            .version(1L)
            .build();

        DefendantAccountHeaderSummary dto = mapper.toDto(entity);
        assertEquals("Defendant", dto.getDebtorType());
        assertFalse(dto.getIsYouth());
    }

    @Test
    void testMapToDtoCoversFields() {
        DefendantAccountHeaderViewEntity entity = DefendantAccountHeaderViewEntity.builder()
            .partyId(123L)
            .parentGuardianAccountPartyId(456L)
            .accountNumber("ACCT100")
            .accountType(DefendantAccountType.FINES)
            .prosecutorCaseReference("PCR1")
            .fixedPenaltyTicketNumber("FPT1")
            .accountStatus(DefendantAccountStatus.LIVE)
            .businessUnitId((short) 77)
            .businessUnitName("BUName")
            .businessUnitCode("BU77")
            .imposed(BigDecimal.valueOf(11))
            .arrears(BigDecimal.valueOf(22))
            .paid(BigDecimal.valueOf(33))
            .accountBalance(BigDecimal.valueOf(44))
            .organisation(false)
            .organisationName("MyOrg")
            .title("Mr")
            .firstnames("Robo")
            .surname("Cop")
            .birthDate(LocalDate.now().minusYears(10))
            .version(1L)
            .build();

        DefendantAccountHeaderSummary dto = mapper.toDto(entity);
        assertEquals("ACCT100", dto.getAccountNumber());
        assertNotNull(dto.getPartyDetails());
        assertEquals("BU77", dto.getBusinessUnitSummary().getBusinessUnitCode());
    }

    @Test
    void testMapToDto_DefendantPartyId_ComesFromDefendantAccountPartyId() {
        DefendantAccountHeaderViewEntity entity = DefendantAccountHeaderViewEntity.builder()
            .defendantAccountId(77L)
            .defendantAccountPartyId(77L)
            .partyId(999L)
            .accountNumber("177A")
            .accountStatus(DefendantAccountStatus.LIVE)
            .version(1L)
            .build();

        DefendantAccountHeaderSummary dto = mapper.toDto(entity);

        assertNotNull(dto);
        assertEquals("77", dto.getDefendantAccountPartyId());
        assertNotEquals("999", dto.getDefendantAccountPartyId());
    }

    @Test
    void testMapToDto_DefendantPartyId_NullWhenDefendantAccountPartyIdIsNull() {
        DefendantAccountHeaderViewEntity entity = DefendantAccountHeaderViewEntity.builder()
            .defendantAccountId(88L)
            .defendantAccountPartyId(null)
            .accountStatus(DefendantAccountStatus.LIVE)
            .version(1L)
            .build();

        DefendantAccountHeaderSummary dto = mapper.toDto(entity);

        assertNull(dto.getDefendantAccountPartyId());
    }

    @Test
    void testMapToDto_NormalisesAccountTypeAndStatusDisplayName() {
        DefendantAccountHeaderViewEntity entity = DefendantAccountHeaderViewEntity.builder()
            .defendantAccountId(77L)
            .defendantAccountPartyId(77L)
            .accountNumber("177A")
            .accountType(DefendantAccountType.FINES)
            .accountStatus(DefendantAccountStatus.LIVE)
            .version(1L)
            .build();

        DefendantAccountHeaderSummary dto = mapper.toDto(entity);

        assertEquals("Fine", dto.getAccountType());
        assertEquals("Live", dto.getAccountStatusReference().getAccountStatusDisplayName());
    }

    @Test
    void given_account_status_cs_when_mapping_then_status_code_and_display_name_are_correct() {
        DefendantAccountHeaderViewEntity entity = DefendantAccountHeaderViewEntity.builder()
            .defendantAccountId(77L)
            .defendantAccountPartyId(77L)
            .accountStatus(DefendantAccountStatus.ACCOUNT_CONSOLIDATED)
            .version(1L)
            .build();

        DefendantAccountHeaderSummary dto = mapper.toDto(entity);

        assertEquals("CS", dto.getAccountStatusReference().getAccountStatusCode());
        assertEquals("Account consolidated", dto.getAccountStatusReference().getAccountStatusDisplayName());
    }

    @Test
    void given_parent_guardian_party_id_when_mapping_then_parent_guardian_party_id_is_stringified() {
        DefendantAccountHeaderViewEntity entity = DefendantAccountHeaderViewEntity.builder()
            .defendantAccountId(77L)
            .defendantAccountPartyId(77L)
            .parentGuardianAccountPartyId(88L)
            .accountStatus(DefendantAccountStatus.LIVE)
            .version(1L)
            .build();

        DefendantAccountHeaderSummary dto = mapper.toDto(entity);

        assertEquals("88", dto.getParentGuardianPartyId());
    }

    @Test
    void given_null_account_type_when_mapping_then_account_type_is_null() {
        DefendantAccountHeaderViewEntity entity = DefendantAccountHeaderViewEntity.builder()
            .defendantAccountId(77L)
            .defendantAccountPartyId(77L)
            .accountType(null)
            .accountStatus(DefendantAccountStatus.LIVE)
            .version(1L)
            .build();

        DefendantAccountHeaderSummary dto = mapper.toDto(entity);

        assertNull(dto.getAccountType());
    }

    @Test
    void given_null_payment_amounts_when_mapping_then_payment_state_amounts_are_zero() {
        DefendantAccountHeaderViewEntity entity = DefendantAccountHeaderViewEntity.builder()
            .defendantAccountId(77L)
            .defendantAccountPartyId(77L)
            .accountStatus(DefendantAccountStatus.LIVE)
            .imposed(null)
            .arrears(null)
            .paid(null)
            .accountBalance(null)
            .version(1L)
            .build();

        DefendantAccountHeaderSummary dto = mapper.toDto(entity);

        assertEquals(BigDecimal.ZERO, dto.getPaymentStateSummary().getImposedAmount());
        assertEquals(BigDecimal.ZERO, dto.getPaymentStateSummary().getArrearsAmount());
        assertEquals(BigDecimal.ZERO, dto.getPaymentStateSummary().getPaidAmount());
        assertEquals(BigDecimal.ZERO, dto.getPaymentStateSummary().getAccountBalance());
    }

    @Test
    void given_organisation_party_when_mapping_then_organisation_details_are_populated() {
        DefendantAccountHeaderViewEntity entity = DefendantAccountHeaderViewEntity.builder()
            .defendantAccountId(77L)
            .defendantAccountPartyId(77L)
            .partyId(101L)
            .organisation(true)
            .organisationName("Acme Ltd")
            .accountStatus(DefendantAccountStatus.LIVE)
            .version(1L)
            .build();

        DefendantAccountHeaderSummary dto = mapper.toDto(entity);

        assertTrue(dto.getPartyDetails().getOrganisationFlag());
        assertEquals("101", dto.getPartyDetails().getPartyId());
        assertEquals("Acme Ltd", dto.getPartyDetails().getOrganisationDetails().getOrganisationName());
        assertNull(dto.getPartyDetails().getIndividualDetails());
    }

    @Test
    void testMapToDto_SerialisedStructureMatchesApiFields() throws Exception {
        DefendantAccountHeaderViewEntity entity = DefendantAccountHeaderViewEntity.builder()
            .defendantAccountId(77L)
            .defendantAccountPartyId(77L)
            .partyId(77L)
            .accountNumber("177A")
            .organisation(false)
            .firstnames("Anna")
            .surname("Graham")
            .accountStatus(DefendantAccountStatus.LIVE)
            .version(1L)
            .build();

        DefendantAccountHeaderSummary dto = mapper.toDto(entity);
        String json = new ObjectMapper().writeValueAsString(dto);

        assertTrue(json.contains("\"defendant_account_party_id\""));
        assertTrue(json.contains("\"party_details\""));
        assertTrue(json.contains("\"account_number\""));
    }
}
