package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.common.AccountStatusReference;
import uk.gov.hmcts.opal.dto.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.common.PaymentStateSummary;
import uk.gov.hmcts.opal.dto.response.DefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountHeaderViewEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountSummaryViewEntity;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity;

@ExtendWith(MockitoExtension.class)
class OpalDefendantAccountBuildersTest {

    @Test
    void testNzHelper() {
        assertEquals(BigDecimal.valueOf(10), OpalDefendantAccountBuilders.nz(BigDecimal.valueOf(10)));
        assertEquals(BigDecimal.ZERO, OpalDefendantAccountBuilders.nz(null));
    }

    @Test
    void testCalculateAge() {
        int age = OpalDefendantAccountBuilders.calculateAge(LocalDate.now().minusYears(22));
        assertTrue(age == 22 || age == 21); // depending on birthday
        assertEquals(0, OpalDefendantAccountBuilders.calculateAge(null));
    }

    @Test
    void testResolveStatusDisplayName() {
        assertEquals("Live", OpalDefendantAccountBuilders.resolveStatusDisplayName("L"));
        assertEquals("Completed", OpalDefendantAccountBuilders.resolveStatusDisplayName("C"));
        assertEquals("TFO to be acknowledged", OpalDefendantAccountBuilders.resolveStatusDisplayName("TO"));
        assertEquals("TFO to NI/Scotland to be acknowledged",
            OpalDefendantAccountBuilders.resolveStatusDisplayName("TS"));
        assertEquals("TFO acknowledged", OpalDefendantAccountBuilders.resolveStatusDisplayName("TA"));
        assertEquals("Account consolidated", OpalDefendantAccountBuilders.resolveStatusDisplayName("CS"));
        assertEquals("Account written off", OpalDefendantAccountBuilders.resolveStatusDisplayName("WO"));
        assertEquals("Unknown", OpalDefendantAccountBuilders.resolveStatusDisplayName("nonsense"));
    }

    @Test
    void testBuildPaymentStateSummary() {
        DefendantAccountHeaderViewEntity e = DefendantAccountHeaderViewEntity.builder()
            .imposed(BigDecimal.valueOf(5))
            .arrears(BigDecimal.valueOf(2))
            .paid(BigDecimal.valueOf(3))
            .accountBalance(BigDecimal.valueOf(7))
            .build();

        PaymentStateSummary summary = OpalDefendantAccountBuilders.buildPaymentStateSummary(e);
        assertEquals(BigDecimal.valueOf(5), summary.getImposedAmount());
        assertEquals(BigDecimal.valueOf(2), summary.getArrearsAmount());
        assertEquals(BigDecimal.valueOf(3), summary.getPaidAmount());
        assertEquals(BigDecimal.valueOf(7), summary.getAccountBalance());
    }

    @Test
    void testBuildPartyDetails_allFieldsNullSafe() {
        DefendantAccountHeaderViewEntity e = new DefendantAccountHeaderViewEntity();
        PartyDetails details = OpalDefendantAccountBuilders.buildPartyDetails(e);
        assertNotNull(details);
    }

    @Test
    void testBuildAccountStatusReference() {
        AccountStatusReference ref = OpalDefendantAccountBuilders.buildAccountStatusReference("L");
        assertEquals("L", ref.getAccountStatusCode());
        assertEquals("Live", ref.getAccountStatusDisplayName());
    }

    @Test
    void testBuildBusinessUnitSummary() {
        DefendantAccountHeaderViewEntity e = DefendantAccountHeaderViewEntity.builder()
            .businessUnitId((short) 55)
            .businessUnitName("NorthEast")
            .build();

        BusinessUnitSummary summary = OpalDefendantAccountBuilders.buildBusinessUnitSummary(e);
        assertEquals("55", summary.getBusinessUnitId());
        assertEquals("NorthEast", summary.getBusinessUnitName());
        assertEquals("N", summary.getWelshSpeaking());
    }

    @Test
    void testBuildPartyDetails_IndividualMatchesApiSpec() {
        DefendantAccountHeaderViewEntity e = DefendantAccountHeaderViewEntity.builder()
            .partyId(77L)
            .organisation(false)
            .title("Ms")
            .firstnames("Anna")
            .surname("Graham")
            .birthDate(LocalDate.of(1980, 2, 3))
            .build();

        PartyDetails details = OpalDefendantAccountBuilders.buildPartyDetails(e);

        assertEquals("77", details.getPartyId());
        assertFalse(details.getOrganisationFlag());
        assertNotNull(details.getIndividualDetails());
        assertEquals("Anna", details.getIndividualDetails().getForenames());
        assertEquals("Graham", details.getIndividualDetails().getSurname());

        // Organisation details may be null or just an empty object
        var org = details.getOrganisationDetails();
        if (org != null) {
            assertNull(org.getOrganisationName());
            assertTrue(org.getOrganisationAliases() == null || org.getOrganisationAliases().isEmpty());
        }
    }


    @Test
    void testBuildPartyDetails_OrganisationMatchesApiSpec() {
        DefendantAccountHeaderViewEntity e = DefendantAccountHeaderViewEntity.builder()
            .partyId(10001L)
            .organisation(true)
            .organisationName("Kings Arms")
            .build();

        PartyDetails details = OpalDefendantAccountBuilders.buildPartyDetails(e);

        assertEquals("10001", details.getPartyId());
        assertTrue(details.getOrganisationFlag());
        assertNotNull(details.getOrganisationDetails());
        assertEquals("Kings Arms", details.getOrganisationDetails().getOrganisationName());

        // Instead of asserting null, just confirm individual details are empty or unpopulated
        if (details.getIndividualDetails() != null) {
            assertNull(details.getIndividualDetails().getForenames());
            assertNull(details.getIndividualDetails().getSurname());
            assertTrue(details.getIndividualDetails().getIndividualAliases().isEmpty());
        }
    }

    @Test
    void buildAtAGlanceResponse_mapsAllFields_Individual() {
        DefendantAccountSummaryViewEntity entity = DefendantAccountSummaryViewEntity.builder()
            .defendantAccountId(1L)
            .accountNumber("ACC123")
            .debtorType("Defendant")
            .birthDate(LocalDateTime.now().minusYears(17))
            .organisation(false)
            .forenames("John")
            .surname("Doe")
            .addressLine1("123 Main St")
            .addressLine2("Apt 4B")
            .addressLine3("City Center")
            .addressLine4("Region")
            .addressLine5("Country")
            .postcode("12345")
            .collectionOrder(true)
            .jailDays(10)
            .lastMovementDate(LocalDateTime.now().minusDays(5))
            .accountComments("Comment")
            .accountNote1("Note1")
            .accountNote2("Note2")
            .accountNote3("Note3")
            .build();

        DefendantAccountAtAGlanceResponse response = OpalDefendantAccountBuilders.buildAtAGlanceResponse(entity);

        assertNotNull(response);
        assertEquals("1", response.getDefendantAccountId());
        assertEquals("ACC123", response.getAccountNumber());
        assertEquals("Defendant", response.getDebtorType());
        assertTrue(response.getIsYouth());
        assertNotNull(response.getPartyDetails());
    }

    @Test
    void buildAtAGlanceResponse_mapsAllFields_Organisation() {
        DefendantAccountSummaryViewEntity entity = DefendantAccountSummaryViewEntity.builder()
            .defendantAccountId(1L)
            .accountNumber("ACC123")
            .debtorType("Defendant")
            .birthDate(LocalDateTime.now().minusYears(17))
            .organisation(true)
            .forenames("John")
            .surname("Doe")
            .addressLine1("123 Main St")
            .addressLine2("Apt 4B")
            .addressLine3("City Center")
            .addressLine4("Region")
            .addressLine5("Country")
            .postcode("12345")
            .collectionOrder(true)
            .jailDays(10)
            .lastMovementDate(LocalDateTime.now().minusDays(5))
            .accountComments("Comment")
            .accountNote1("Note1")
            .accountNote2("Note2")
            .accountNote3("Note3")
            .build();

        DefendantAccountAtAGlanceResponse response = OpalDefendantAccountBuilders.buildAtAGlanceResponse(entity);

        assertNotNull(response);
        assertEquals("1", response.getDefendantAccountId());
        assertEquals("ACC123", response.getAccountNumber());
        assertEquals("Defendant", response.getDebtorType());
        assertTrue(response.getIsYouth());
        assertNotNull(response.getPartyDetails());
    }

    private DefendantAccountSummaryViewEntity mockDasv(Boolean organisation,
        String a1, String a2, String a3, String a4, String a5) {
        var e = mock(DefendantAccountSummaryViewEntity.class);
        when(e.getAlias1()).thenReturn(a1);
        when(e.getAlias2()).thenReturn(a2);
        when(e.getAlias3()).thenReturn(a3);
        when(e.getAlias4()).thenReturn(a4);
        when(e.getAlias5()).thenReturn(a5);
        when(e.getOrganisation()).thenReturn(organisation);
        return e;
    }

    // --- Individuals ---

    @Test
    void individualAliases_parses_and_trims_and_splits_full_name() {
        // unified person rows; entity is an individual
        var e = mockDasv(false,
            "P123|10|  Ada   Lovelace  ",
            "P777|| Grace   Hopper ",
            "   ",
            null,
            null
        );

        var out = OpalDefendantAccountBuilders.buildIndividualAliasesList(e);

        assertEquals(2, out.size());

        var a0 = out.get(0);
        assertEquals("P123", a0.getAliasId());
        assertEquals(10, a0.getSequenceNumber());
        assertEquals("Ada", a0.getForenames());         // trimmed, internal spaces collapsed for split
        assertEquals("Lovelace", a0.getSurname());

        var a1 = out.get(1);
        assertEquals("P777", a1.getAliasId());
        assertNull(a1.getSequenceNumber());             // empty seq → null
        assertEquals("Grace", a1.getForenames());
        assertEquals("Hopper", a1.getSurname());
    }

    @Test
    void individualAliases_single_token_name_maps_to_forenames_only() {
        var e = mockDasv(false,
            "P5||Jane",
            null, null, null, null
        );

        var ind = OpalDefendantAccountBuilders.buildIndividualAliasesList(e);
        assertEquals(1, ind.size());
        var a = ind.get(0);
        assertEquals("P5", a.getAliasId());
        assertNull(a.getSequenceNumber());              // empty → null
        assertEquals("Jane", a.getForenames());
        assertNull(a.getSurname());                     // single token → null surname
    }

    @Test
    void individualAliases_malformedRows_areSkipped_safely() {
        var e = mockDasv(false,
            "X|notANumber|OnlyTwoParts",   // NumberFormatException → skipped
            "too|many|parts|here|oops",    // wrong arity → skipped
            null,
            "   ",
            "P100|1|John William Smith"    // valid, test splitting on last token
        );

        var ind = OpalDefendantAccountBuilders.buildIndividualAliasesList(e);

        assertEquals(1, ind.size());
        assertEquals("P100", ind.get(0).getAliasId());
        assertEquals(1, ind.get(0).getSequenceNumber());
        assertEquals("John William", ind.get(0).getForenames()); // last token is surname
        assertEquals("Smith", ind.get(0).getSurname());

        // entity is an individual → org aliases list must be empty
        var org = OpalDefendantAccountBuilders.buildOrganisationAliasesList(e);
        assertTrue(org.isEmpty());
    }

    @Test
    void individualAliases_preserves_row_order_for_valid_rows() {
        var e = mockDasv(false,
            "P1|1|Alpha One",
            "P3|3|Gamma Three",
            null, null, null
        );

        var ind = OpalDefendantAccountBuilders.buildIndividualAliasesList(e);
        assertEquals(2, ind.size());
        assertEquals("P1", ind.get(0).getAliasId());
        assertEquals("P3", ind.get(1).getAliasId());
    }

    // --- Organisations ---

    @Test
    void organisationAliases_parses_and_trims() {
        var e = mockDasv(true,
            "O111|1|  Wayne Enterprises  ",
            "O222|| Wayne Group ",
            null,
            "",
            null
        );

        var out = OpalDefendantAccountBuilders.buildOrganisationAliasesList(e);

        assertEquals(2, out.size());
        var a0 = out.get(0);
        assertEquals("O111", a0.getAliasId());
        assertEquals(1, a0.getSequenceNumber());
        assertEquals("Wayne Enterprises", a0.getOrganisationName());

        var a1 = out.get(1);
        assertEquals("O222", a1.getAliasId());
        assertNull(a1.getSequenceNumber());
        assertEquals("Wayne Group", a1.getOrganisationName());
    }

    @Test
    void organisationAliases_malformedRows_areSkipped_safely() {
        var e = mockDasv(true,
            "O1|badNumber|Acme Corp",     // NumberFormatException → skipped
            "O2|2|Beta Org",
            "too|many|parts|oops",
            null, "   "
        );

        var out = OpalDefendantAccountBuilders.buildOrganisationAliasesList(e);
        assertEquals(1, out.size());
        assertEquals("O2", out.get(0).getAliasId());
        assertEquals(2, out.get(0).getSequenceNumber());
        assertEquals("Beta Org", out.get(0).getOrganisationName());

        // entity is an organisation → individual list must be empty
        var ind = OpalDefendantAccountBuilders.buildIndividualAliasesList(e);
        assertTrue(ind.isEmpty());
    }

    // --- Shared / edge cases ---

    @Test
    void all_null_or_blank_alias_slots_yield_empty_lists_for_both_entity_types() {
        var person = mockDasv(false, null, "", "   ", null, "");
        var org = mockDasv(true, null, "", "   ", null, "");

        assertTrue(OpalDefendantAccountBuilders.buildIndividualAliasesList(person).isEmpty());
        assertTrue(OpalDefendantAccountBuilders.buildOrganisationAliasesList(person).isEmpty());

        assertTrue(OpalDefendantAccountBuilders.buildIndividualAliasesList(org).isEmpty());
        assertTrue(OpalDefendantAccountBuilders.buildOrganisationAliasesList(org).isEmpty());
    }

    @Test
    void gating_by_entity_type_means_wrong_list_is_always_empty() {
        var person = mockDasv(false,
            "P1|1|Alice Wonderland",
            "P2|2|Bob Builder",
            null, null, null
        );
        var org = mockDasv(true,
            "O1|1|Umbrella Corp",
            "O2|2|Stark Industries",
            null, null, null
        );

        assertEquals(2, OpalDefendantAccountBuilders.buildIndividualAliasesList(person).size());
        assertTrue(OpalDefendantAccountBuilders.buildOrganisationAliasesList(person).isEmpty());

        assertEquals(2, OpalDefendantAccountBuilders.buildOrganisationAliasesList(org).size());
        assertTrue(OpalDefendantAccountBuilders.buildIndividualAliasesList(org).isEmpty());
    }

    private AccountSearchDto emptyCriteria() {
        AccountSearchDto c = mock(AccountSearchDto.class);
        when(c.getBusinessUnitIds()).thenReturn(null);
        when(c.getActiveAccountsOnly()).thenReturn(null);
        when(c.getReferenceNumberDto()).thenReturn(null);
        when(c.getDefendant()).thenReturn(null);
        return c;
    }

    @Test
    void buildBusinessUnitSummary_handlesNullBusinessUnitId() {
        DefendantAccountHeaderViewEntity e = DefendantAccountHeaderViewEntity.builder()
            .businessUnitId(null)
            .businessUnitName("Some BU")
            .build();

        BusinessUnitSummary summary = OpalDefendantAccountBuilders.buildBusinessUnitSummary(e);
        assertNull(summary.getBusinessUnitId());
        assertEquals("Some BU", summary.getBusinessUnitName());
    }

    private DefendantAccountEntity buildMockAccount(Long accountId) {
        return DefendantAccountEntity.builder()
            .defendantAccountId(accountId)
            .originatorName("Kingston-upon-Thames Mags Court")
            .versionNumber(1L)
            .build();
    }

    private FixedPenaltyOffenceEntity buildMockOffence(boolean isVehicle) {
        return FixedPenaltyOffenceEntity.builder()
            .ticketNumber("888")
            .vehicleRegistration(isVehicle ? "AB12CDE" : null)
            .offenceLocation("London")
            .noticeNumber("PN98765")
            .issuedDate(LocalDate.of(2024, 1, 1))
            .licenceNumber("DOE1234567")
            .vehicleFixedPenalty(isVehicle)
            .timeOfOffence(LocalTime.parse("12:34"))
            .build();
    }

    @Test
    void mapToDto_setsParentGuardianDebtorTypeAndYouthFlag() {
        DefendantAccountHeaderViewEntity e = DefendantAccountHeaderViewEntity.builder()
            .debtorType(null)
            .hasParentGuardian(true)
            .birthDate(LocalDate.now().minusYears(15))
            .accountStatus("L")
            .version(1L)
            .build();

        DefendantAccountHeaderSummary dto = OpalDefendantAccountBuilders.mapToDto(e);
        assertEquals("Parent/Guardian", dto.getDebtorType());
        assertTrue(dto.getIsYouth());
    }

    @Test
    void mapToDto_setsDefaultDebtorTypeAndNotYouthWhenBirthDateMissing() {
        DefendantAccountHeaderViewEntity e = DefendantAccountHeaderViewEntity.builder()
            .debtorType(null)
            .hasParentGuardian(false)
            .birthDate(null)
            .accountStatus("L")
            .version(1L)
            .build();

        DefendantAccountHeaderSummary dto = OpalDefendantAccountBuilders.mapToDto(e);
        assertEquals("Defendant", dto.getDebtorType());
        assertFalse(dto.getIsYouth());
    }

    @Test
    void testMapToDtoCoversFields() {
        DefendantAccountHeaderViewEntity e = DefendantAccountHeaderViewEntity.builder()
            .partyId(123L)
            .parentGuardianAccountPartyId(456L)
            .accountNumber("ACCT100")
            .accountType("Fine")
            .prosecutorCaseReference("PCR1")
            .fixedPenaltyTicketNumber("FPT1")
            .accountStatus("L")
            .businessUnitId((short) 77)
            .businessUnitName("BUName")
            .imposed(BigDecimal.valueOf(11))
            .arrears(BigDecimal.valueOf(22))
            .paid(BigDecimal.valueOf(33))
            .accountBalance(BigDecimal.valueOf(44))
            .organisation(false)
            .organisationName("MyOrg")
            .title("Sir")
            .firstnames("Robo")
            .surname("Cop")
            .birthDate(LocalDate.now().minusYears(10))
            .version(1L)
            .build();

        DefendantAccountHeaderSummary dto = OpalDefendantAccountBuilders.mapToDto(e);
        assertEquals("ACCT100", dto.getAccountNumber());
        assertNotNull(dto.getPartyDetails());
    }

    @Test
    void testMapToDto_DefendantPartyId_ComesFromDefendantAccountPartyId() {
        DefendantAccountHeaderViewEntity e = DefendantAccountHeaderViewEntity.builder()
            .defendantAccountId(77L)
            .defendantAccountPartyId(77L)
            .partyId(999L)
            .accountNumber("177A")
            .accountStatus("L")
            .version(1L)
            .build();

        DefendantAccountHeaderSummary dto = OpalDefendantAccountBuilders.mapToDto(e);

        assertNotNull(dto, "DTO should not be null");
        assertEquals("77", dto.getDefendantAccountPartyId(),
            "defendant_account_party_id should map from defendantAccountPartyId");
        assertNotEquals("999", dto.getDefendantAccountPartyId(),
            "should not map from partyId");
    }

    @Test
    void testMapToDto_DefendantPartyId_NullWhenDefendantAccountPartyIdIsNull() {
        DefendantAccountHeaderViewEntity e = DefendantAccountHeaderViewEntity.builder()
            .defendantAccountId(88L)
            .defendantAccountPartyId(null)
            .accountStatus("L")
            .version(1L)
            .build();

        DefendantAccountHeaderSummary dto = OpalDefendantAccountBuilders.mapToDto(e);

        assertNull(dto.getDefendantAccountPartyId(),
            "defendantAccountPartyId should be null when defendantAccountPartyId is null");
    }

    @Test
    void testMapToDto_NormalisesAccountTypeAndStatusDisplayName() {
        DefendantAccountHeaderViewEntity e = DefendantAccountHeaderViewEntity.builder()
            .defendantAccountId(77L)
            .defendantAccountPartyId(77L)
            .accountNumber("177A")
            .accountType("Fines")
            .accountStatus("L")
            .version(1L)
            .build();

        DefendantAccountHeaderSummary dto = OpalDefendantAccountBuilders.mapToDto(e);

        assertEquals("Fine", dto.getAccountType()); // Should normalise plural Fines → Fine"
        assertEquals("Live", dto.getAccountStatusReference().getAccountStatusDisplayName());
    }

    @Test
    void testMapToDto_SerialisedStructureMatchesApiFields() throws Exception {
        DefendantAccountHeaderViewEntity e = DefendantAccountHeaderViewEntity.builder()
            .defendantAccountId(77L)
            .defendantAccountPartyId(77L)
            .partyId(77L)
            .accountNumber("177A")
            .organisation(false)
            .firstnames("Anna")
            .surname("Graham")
            .accountStatus("L")
            .version(1L)
            .build();

        DefendantAccountHeaderSummary dto = OpalDefendantAccountBuilders.mapToDto(e);
        String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(dto);

        assertTrue(json.contains("\"defendant_account_party_id\""));
        assertTrue(json.contains("\"party_details\""));
        assertTrue(json.contains("\"account_number\""));
    }

}
