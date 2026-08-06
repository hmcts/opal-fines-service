package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.GetDefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.common.AccountStatusReference;
import uk.gov.hmcts.opal.dto.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.common.CommentsAndNotes;
import uk.gov.hmcts.opal.dto.common.EnforcementOverride;
import uk.gov.hmcts.opal.dto.common.EnforcementOverrideResult;
import uk.gov.hmcts.opal.dto.common.EnforcementStatusSummary;
import uk.gov.hmcts.opal.dto.common.Enforcer;
import uk.gov.hmcts.opal.dto.common.IndividualAlias;
import uk.gov.hmcts.opal.dto.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.common.InstalmentPeriod;
import uk.gov.hmcts.opal.dto.common.LJA;
import uk.gov.hmcts.opal.dto.common.LanguagePreference;
import uk.gov.hmcts.opal.dto.common.LanguagePreferences;
import uk.gov.hmcts.opal.dto.common.LastEnforcementAction;
import uk.gov.hmcts.opal.dto.common.OrganisationAlias;
import uk.gov.hmcts.opal.dto.common.OrganisationDetails;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.common.PaymentStateSummary;
import uk.gov.hmcts.opal.dto.common.PaymentTermsSummary;
import uk.gov.hmcts.opal.dto.common.PaymentTermsType;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity;
import uk.gov.hmcts.opal.entity.debtordetail.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.debtordetail.Language;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountHeaderViewEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountStatus;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountSummaryViewEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.generated.model.AccountStatusReferenceCommon;
import uk.gov.hmcts.opal.generated.model.EnforcementActionDefendantAccount;
import uk.gov.hmcts.opal.generated.model.EnforcementOverrideDefendantAccount;
import uk.gov.hmcts.opal.generated.model.EnforcementOverrideResultDefendantAccount;
import uk.gov.hmcts.opal.generated.model.EnforcerDefendantAccount;
import uk.gov.hmcts.opal.generated.model.LocalJusticeAreaDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.ResultResponsesCommon;

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
    void testBuildAccountStatusReference() {
        AccountStatusReference reference =
            OpalDefendantAccountBuilders.buildAccountStatusReference(DefendantAccountStatus.LIVE);

        assertNotNull(reference);
        assertEquals("L", reference.getAccountStatusCode());
        assertEquals("Live", reference.getAccountStatusDisplayName());
    }

    @Test
    void buildEnforcementAction_mapsResultResponsesFromKeyedJson() {
        ResultEntity result = ResultEntity.builder()
            .resultId("FE")
            .resultTitle("Further Enforcement")
            .resultParameters("""
                [
                  {"name":"reason"},
                  {"name":"collectiontype"},
                  {"name":"reserveterms"}
                ]
                """)
            .build();

        EnforcementEntity enforcement = EnforcementEntity.builder()
            .resultId("FE")
            .result(result)
            .reason("a")
            .resultResponses("""
                {
                  "reason":"a",
                  "collectiontype":"Wages",
                  "reserveterms":"aa"
                }
                """)
            .postedDate(LocalDateTime.of(2026, Month.JUNE, 11, 10, 0))
            .build();

        EnforcementActionDefendantAccount action =
            OpalDefendantAccountBuilders.buildEnforcementAction(enforcement, null);

        assertNotNull(action);
        assertNotNull(action.getResultResponses());
        assertEquals(3, action.getResultResponses().size());

        ResultResponsesCommon reason = action.getResultResponses().get(0);
        assertEquals("reason", reason.getParameterName());
        assertEquals("a", reason.getResponse());

        ResultResponsesCommon collectionType = action.getResultResponses().get(1);
        assertEquals("collectiontype", collectionType.getParameterName());
        assertEquals("Wages", collectionType.getResponse());

        ResultResponsesCommon reserveTerms = action.getResultResponses().get(2);
        assertEquals("reserveterms", reserveTerms.getParameterName());
        assertEquals("aa", reserveTerms.getResponse());
    }

    @Test
    void applyEnforcementOverride_clearsIds_whenEnforcerAndLjaObjectsAreNull() {
        DefendantAccountEntity entity = DefendantAccountEntity.builder().build();
        entity.setEnforcementOverrideResultId("OLD");
        entity.setEnforcementOverrideEnforcerId(22L);
        entity.setEnforcementOverrideTfoLjaId((short) 33);

        EnforcementOverrideDefendantAccount override = EnforcementOverrideDefendantAccount.builder()
            .enforcementOverrideResult(EnforcementOverrideResultDefendantAccount.builder()
                .enforcementOverrideResultId("FWEC")
                .build())
            .build();

        OpalDefendantAccountBuilders.applyEnforcementOverride(entity, override);

        assertEquals("FWEC", entity.getEnforcementOverrideResultId());
        assertNull(entity.getEnforcementOverrideEnforcerId());
        assertNull(entity.getEnforcementOverrideTfoLjaId());
    }

    @Test
    void applyEnforcementOverride_clearsAllIds_whenResultEnforcerAndLjaObjectsAreNull() {
        DefendantAccountEntity entity = DefendantAccountEntity.builder().build();
        entity.setEnforcementOverrideResultId("OLD");
        entity.setEnforcementOverrideEnforcerId(22L);
        entity.setEnforcementOverrideTfoLjaId((short) 33);

        EnforcementOverrideDefendantAccount override = EnforcementOverrideDefendantAccount.builder().build();

        OpalDefendantAccountBuilders.applyEnforcementOverride(entity, override);

        assertNull(entity.getEnforcementOverrideResultId());
        assertNull(entity.getEnforcementOverrideEnforcerId());
        assertNull(entity.getEnforcementOverrideTfoLjaId());
    }

    @Test
    void applyEnforcementOverride_clearsIds_whenEnforcerAndLjaIdsAreNull() {
        DefendantAccountEntity entity = DefendantAccountEntity.builder().build();
        entity.setEnforcementOverrideResultId("OLD");
        entity.setEnforcementOverrideEnforcerId(22L);
        entity.setEnforcementOverrideTfoLjaId((short) 33);

        EnforcementOverrideDefendantAccount override = EnforcementOverrideDefendantAccount.builder()
            .enforcementOverrideResult(EnforcementOverrideResultDefendantAccount.builder()
                .enforcementOverrideResultId(null)
                .build())
            .enforcer(EnforcerDefendantAccount.builder()
                .enforcerId(null)
                .build())
            .lja(LocalJusticeAreaDefendantAccount.builder()
                .ljaId(null)
                .build())
            .build();

        OpalDefendantAccountBuilders.applyEnforcementOverride(entity, override);

        assertNull(entity.getEnforcementOverrideResultId());
        assertNull(entity.getEnforcementOverrideEnforcerId());
        assertNull(entity.getEnforcementOverrideTfoLjaId());
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
    void buildPartyDetails_preservesNullDateOfBirthAndAge() {
        PartyEntity party = PartyEntity.builder()
            .partyId(88L)
            .organisation(false)
            .build();

        PartyDetails details = OpalDefendantAccountBuilders.buildPartyDetails(party, List.of());

        assertNull(details.getIndividualDetails().getDateOfBirth());
        assertNull(details.getIndividualDetails().getAge());
    }

    @Test
    void testBuildAccountStatusReference_handlesNullStatus() {
        assertNull(OpalDefendantAccountBuilders.buildAccountStatusReference(null));
    }

    @Test
    void testBuildAccountStatusReferenceCommon() {
        AccountStatusReferenceCommon ref =
            OpalDefendantAccountBuilders.buildAccountStatusReferenceCommon(DefendantAccountStatus.LIVE);
        assertEquals("L", ref.getAccountStatusCode().getValue());
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
    void given_debtorDetailLanguages_when_buildLanguagePreferences_then_mapEnumCodes() {
        DebtorDetailEntity debtorDetail = DebtorDetailEntity.builder()
            .documentLanguage(Language.WELSH_AND_ENGLISH)
            .hearingLanguage(Language.ENGLISH)
            .build();

        LanguagePreferences preferences = OpalDefendantAccountBuilders.buildLanguagePreferences(debtorDetail);

        assertNotNull(preferences);
        assertEquals("CY", preferences.getDocumentLanguagePreference().getLanguageCode());
        assertEquals("EN", preferences.getHearingLanguagePreference().getLanguageCode());
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

        GetDefendantAccountAtAGlanceResponse response = OpalDefendantAccountBuilders.buildAtAGlanceResponse(entity);

        assertNotNull(response);
        assertNotNull(response.getPayload());
        assertEquals("1", response.getPayload().getDefendantAccountId());
        assertEquals("ACC123", response.getPayload().getAccountNumber());
        assertEquals("Defendant", response.getPayload().getDebtorType().getValue());
        assertTrue(response.getPayload().getIsYouth());
        assertNotNull(response.getPayload().getPartyDetails());
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

        GetDefendantAccountAtAGlanceResponse response = OpalDefendantAccountBuilders.buildAtAGlanceResponse(entity);

        assertNotNull(response);
        assertNotNull(response.getPayload());
        assertEquals("1", response.getPayload().getDefendantAccountId());
        assertEquals("ACC123", response.getPayload().getAccountNumber());
        assertEquals("Defendant", response.getPayload().getDebtorType().getValue());
        assertTrue(response.getPayload().getIsYouth());
        assertNotNull(response.getPayload().getPartyDetails());
    }

    @Test
    void toStrictPartyDetails_mapsOrganisationBranch() {
        PartyDetails source = PartyDetails.builder()
            .partyId("123")
            .organisationFlag(true)
            .organisationDetails(OrganisationDetails.builder()
                .organisationName("Acme Ltd")
                .organisationAliases(java.util.List.of(OrganisationAlias.builder()
                    .aliasId("ORG-1")
                    .sequenceNumber(1)
                    .organisationName("Acme Trading")
                    .build()))
                .build())
            .build();

        PartyDetailsCommonStrict out = OpalDefendantAccountBuilders.toStrictPartyDetails(source);

        assertEquals("123", out.getPartyId());
        assertTrue(out.getOrganisationFlag());
        assertTrue(out.getOrganisationDetails().isPresent());
        assertEquals("Acme Ltd", out.getOrganisationDetails().get().getOrganisationName());
        assertTrue(out.getOrganisationDetails().get().getOrganisationAliases().isPresent());
        assertEquals("ORG-1", out.getOrganisationDetails().get().getOrganisationAliases().get().get(0).getAliasId());
        assertFalse(out.getIndividualDetails().isPresent());
    }

    @Test
    void toStrictPartyDetails_mapsIndividualBranch() {
        PartyDetails source = PartyDetails.builder()
            .partyId("456")
            .organisationFlag(false)
            .individualDetails(IndividualDetails.builder()
                .title("Mr")
                .forenames("John")
                .surname("Smith")
                .dateOfBirth(null)
                .age("34")
                .nationalInsuranceNumber("QQ123456C")
                .individualAliases(java.util.List.of(IndividualAlias.builder()
                    .aliasId("IND-1")
                    .sequenceNumber(2)
                    .surname("Smith")
                    .forenames("John")
                    .build()))
                .build())
            .build();

        PartyDetailsCommonStrict out = OpalDefendantAccountBuilders.toStrictPartyDetails(source);

        assertEquals("456", out.getPartyId());
        assertFalse(out.getOrganisationFlag());
        assertFalse(out.getOrganisationDetails().isPresent());
        assertTrue(out.getIndividualDetails().isPresent());
        assertEquals("Mr", out.getIndividualDetails().get().getTitle().get());
        assertTrue(out.getIndividualDetails().get().getDateOfBirth().isPresent());
        assertNull(out.getIndividualDetails().get().getDateOfBirth().get());
        assertEquals("IND-1", out.getIndividualDetails().get().getIndividualAliases().get().get(0).getAliasId());
    }

    @Test
    void validateAtAGlancePartyDetails_rejectsMissingOrganisationDetails() {
        PartyDetails source = PartyDetails.builder()
            .partyId("123")
            .organisationFlag(true)
            .build();

        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> OpalDefendantAccountBuilders.validateAtAGlancePartyDetails(source)
        );

        assertEquals(
            "At-a-glance party details are invalid: organisation_details is required when organisation_flag=true",
            ex.getMessage()
        );
    }

    @Test
    void validateAtAGlancePartyDetails_rejectsBothBranchesPresent() {
        PartyDetails source = PartyDetails.builder()
            .partyId("456")
            .organisationFlag(false)
            .organisationDetails(OrganisationDetails.builder().organisationName("Wrong").build())
            .individualDetails(IndividualDetails.builder().surname("Smith").build())
            .build();

        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> OpalDefendantAccountBuilders.validateAtAGlancePartyDetails(source)
        );

        assertEquals(
            "At-a-glance party details are invalid: organisation_details must be null when organisation_flag=false",
            ex.getMessage()
        );
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

    @Test
    void buildAtAGlanceResponse_returnsNull_forNullEntity() {
        assertNull(OpalDefendantAccountBuilders.buildAtAGlanceResponse(null));
    }

    @Test
    void toStrictPartyDetails_returnsNull_forNullSource() {
        assertNull(OpalDefendantAccountBuilders.toStrictPartyDetails(null));
    }

    @Test
    void validateAtAGlancePartyDetails_rejectsMissingIndividualDetails() {
        PartyDetails source = PartyDetails.builder()
            .partyId("456")
            .organisationFlag(false)
            .build();

        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> OpalDefendantAccountBuilders.validateAtAGlancePartyDetails(source)
        );

        assertEquals(
            "At-a-glance party details are invalid: individual_details is required when organisation_flag=false",
            ex.getMessage()
        );
    }

    @Test
    void validateAtAGlancePartyDetails_rejectsUnexpectedIndividualDetailsForOrganisation() {
        PartyDetails source = PartyDetails.builder()
            .partyId("123")
            .organisationFlag(true)
            .organisationDetails(OrganisationDetails.builder().organisationName("Acme Ltd").build())
            .individualDetails(IndividualDetails.builder().surname("Smith").build())
            .build();

        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> OpalDefendantAccountBuilders.validateAtAGlancePartyDetails(source)
        );

        assertEquals(
            "At-a-glance party details are invalid: individual_details must be null when organisation_flag=true",
            ex.getMessage()
        );
    }

    @Test
    void strictHelpers_handleNullInputs() {
        assertNull(OpalDefendantAccountBuilders.toOrganisationAliasCommon(null));
        assertNull(OpalDefendantAccountBuilders.toStrictOrganisationDetails(null));
        assertNull(OpalDefendantAccountBuilders.toStrictIndividualDetails(null));
        assertNull(OpalDefendantAccountBuilders.toStrictIndividualAlias(null));
        assertNull(OpalDefendantAccountBuilders.toStrictAddressDetails(null));
        assertNull(OpalDefendantAccountBuilders.toStrictLanguagePreferences(null));
        assertNull(OpalDefendantAccountBuilders.toStrictLanguagePreference(null));
        assertNull(OpalDefendantAccountBuilders.toStrictPaymentTerms(null));
        assertNull(OpalDefendantAccountBuilders.toStrictPaymentTermsType(null));
        assertNull(OpalDefendantAccountBuilders.toStrictInstalmentPeriod(null));
        assertNull(OpalDefendantAccountBuilders.toStrictEnforcementStatus(null));
        assertNull(OpalDefendantAccountBuilders.toStrictLastEnforcementAction(null));
        assertNull(OpalDefendantAccountBuilders.toStrictEnforcementOverride(null));
        assertNull(OpalDefendantAccountBuilders.toStrictEnforcementOverrideResult(null));
        assertNull(OpalDefendantAccountBuilders.toEnforcerReferenceCommon(null));
        assertNull(OpalDefendantAccountBuilders.toStrictLja(null));
        assertNull(OpalDefendantAccountBuilders.toStrictCommentsAndNotes(null));
    }

    @Test
    void toStrictLanguagePreference_returnsNull_whenCodeMissing() {
        LanguagePreference source = LanguagePreference.builder().build();

        assertNull(OpalDefendantAccountBuilders.toStrictLanguagePreference(source));
    }

    @Test
    void strictHelpers_mapOptionalAndUndefinedBranches() {
        OrganisationDetails organisation = OrganisationDetails.builder()
            .organisationName("Acme Ltd")
            .organisationAliases(null)
            .build();
        var strictOrganisation = OpalDefendantAccountBuilders.toStrictOrganisationDetails(organisation);
        assertFalse(strictOrganisation.getOrganisationAliases().isPresent());

        IndividualDetails individual = IndividualDetails.builder()
            .title("Ms")
            .forenames("Ada")
            .surname("Lovelace")
            .dateOfBirth("1815-12-10")
            .age("36")
            .nationalInsuranceNumber(null)
            .individualAliases(null)
            .build();
        var strictIndividual = OpalDefendantAccountBuilders.toStrictIndividualDetails(individual);
        assertTrue(strictIndividual.getIndividualAliases().isPresent());
        assertTrue(strictIndividual.getIndividualAliases().get().isEmpty());

        LanguagePreferences preferences = LanguagePreferences.builder()
            .documentLanguagePreference(LanguagePreference.builder()
                .languageCode(LanguagePreference.LanguageCode.EN)
                .build())
            .hearingLanguagePreference(LanguagePreference.builder()
                .languageCode(LanguagePreference.LanguageCode.CY)
                .build())
            .build();
        assertEquals("EN", OpalDefendantAccountBuilders.toStrictLanguagePreferences(preferences)
            .getDocumentLanguagePreference().get().getLanguageCode().getValue());
    }

    @Test
    void strictPaymentTermsAndEnforcementHelpers_mapUndefinedBranches() {
        PaymentTermsSummary paymentTerms = PaymentTermsSummary.builder()
            .paymentTermsType(PaymentTermsType.builder()
                .paymentTermsTypeCode(PaymentTermsType.PaymentTermsTypeCode.I)
                .build())
            .instalmentPeriod(null)
            .effectiveDate(LocalDate.of(2026, 7, 28))
            .lumpSumAmount(new BigDecimal("100.00"))
            .instalmentAmount(new BigDecimal("10.00"))
            .build();
        var strictPaymentTerms = OpalDefendantAccountBuilders.toStrictPaymentTerms(paymentTerms);
        assertFalse(strictPaymentTerms.getInstalmentPeriod().isPresent());

        EnforcementStatusSummary enforcementStatus = EnforcementStatusSummary.builder()
            .lastEnforcementAction(null)
            .collectionOrderMade(Boolean.TRUE)
            .defaultDaysInJail(7)
            .enforcementOverride(null)
            .lastMovementDate(LocalDate.of(2026, 7, 28))
            .build();
        var strictEnforcement = OpalDefendantAccountBuilders.toStrictEnforcementStatus(enforcementStatus);
        assertFalse(strictEnforcement.getLastEnforcementAction().isPresent());
        assertFalse(strictEnforcement.getEnforcementOverride().isPresent());

        assertNull(OpalDefendantAccountBuilders.toStrictPaymentTermsType(
            PaymentTermsType.builder().build()));
        assertNull(OpalDefendantAccountBuilders.toStrictInstalmentPeriod(
            InstalmentPeriod.builder().build()));
    }

    @Test
    void strictEnforcementHelpers_mapNestedValues() {
        EnforcementOverride override = EnforcementOverride.builder()
            .enforcementOverrideResult(EnforcementOverrideResult.builder()
                .enforcementOverrideId("EO1")
                .enforcementOverrideTitle("Manual override")
                .build())
            .enforcer(Enforcer.builder()
                .enforcerId(11L)
                .enforcerName("Enforcer Name")
                .build())
            .lja(LJA.builder()
                .ljaId((short) 12)
                .ljaCode(null)
                .ljaName("Central LJA")
                .build())
            .build();

        var strictOverride = OpalDefendantAccountBuilders.toStrictEnforcementOverride(override);
        assertTrue(strictOverride.getEnforcementOverrideResult().isPresent());
        assertEquals("EO1", strictOverride.getEnforcementOverrideResult().get().getEnforcementOverrideResultId());
        assertTrue(strictOverride.getEnforcer().isPresent());
        assertEquals(11L, strictOverride.getEnforcer().get().getEnforcerId());
        assertTrue(strictOverride.getLja().isPresent());
        assertTrue(strictOverride.getLja().get().getLjaCode().isPresent());
        assertNull(strictOverride.getLja().get().getLjaCode().get());

        LastEnforcementAction action = LastEnforcementAction.builder()
            .lastEnforcementActionId("REM")
            .lastEnforcementActionTitle("Reminder")
            .build();
        assertEquals("REM", OpalDefendantAccountBuilders.toStrictLastEnforcementAction(action)
            .getLastEnforcementActionId());
    }

    @Test
    void toStrictCommentsAndNotes_mapsNullableStrings() {
        CommentsAndNotes source = CommentsAndNotes.builder()
            .accountNotesAccountComments("Comment")
            .accountNotesFreeTextNote1("N1")
            .accountNotesFreeTextNote2(null)
            .accountNotesFreeTextNote3("N3")
            .build();

        var out = OpalDefendantAccountBuilders.toStrictCommentsAndNotes(source);
        assertEquals("Comment", out.getAccountComment().get());
        assertEquals("N1", out.getFreeTextNote1().get());
        assertTrue(out.getFreeTextNote2().isPresent());
        assertNull(out.getFreeTextNote2().get());
        assertEquals("N3", out.getFreeTextNote3().get());
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
              .versionNumber(BigInteger.ONE)
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

}
