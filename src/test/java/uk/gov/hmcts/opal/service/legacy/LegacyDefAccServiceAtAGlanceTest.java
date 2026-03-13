package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.common.LastEnforcementAction;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType;
import uk.gov.hmcts.opal.dto.legacy.AddressDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.legacy.common.CommentsAndNotes;
import uk.gov.hmcts.opal.dto.legacy.common.EnforcementStatusSummary;
import uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LanguagePreferences;
import uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails;
import uk.gov.hmcts.opal.dto.legacy.common.PaymentTermsSummary;
import uk.gov.hmcts.opal.dto.response.DefendantAccountAtAGlanceResponse;

class LegacyDefAccServiceAtAGlanceTest extends AbstractLegacyDefAccServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void getAtAGlance_success_mapsTopLevelAndNulls() {
        LegacyGetDefendantAccountAtAGlanceResponse body = LegacyGetDefendantAccountAtAGlanceResponse.builder()
            .defendantAccountId("123")
            .accountNumber("ACC-42")
            .debtorType("PERSON")
            .youth(Boolean.FALSE)
            .version(5L)
            .build();

        ParameterizedTypeReference<LegacyGetDefendantAccountAtAGlanceResponse> typeRef =
            new ParameterizedTypeReference<>() {};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(body);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(body.toXml(), HttpStatus.OK));

        DefendantAccountAtAGlanceResponse out = legacyDefendantAccountService.getAtAGlance(123L);

        assertNotNull(out);
        assertEquals("123", out.getDefendantAccountId());
        assertEquals("ACC-42", out.getAccountNumber());
        assertEquals("PERSON", out.getDebtorType());
        assertEquals(Boolean.FALSE, out.getIsYouth());
        assertEquals(BigInteger.valueOf(5L), out.getVersion());
        assertNull(out.getPartyDetails());
        assertNull(out.getAddressDetails());
        assertNull(out.getLanguagePreferences());
        assertNull(out.getPaymentTermsSummary());
        assertNull(out.getEnforcementStatus());
        assertNull(out.getCommentsAndNotes());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAtAGlance_legacyFailure5xx_withEntity_mapsAnyway() {
        LegacyGetDefendantAccountAtAGlanceResponse body =
            LegacyGetDefendantAccountAtAGlanceResponse.builder().version(0L).defendantAccountId("456").build();

        ParameterizedTypeReference<LegacyGetDefendantAccountAtAGlanceResponse> typeRef =
            new ParameterizedTypeReference<>() {};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(body);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(body.toXml(), HttpStatus.SERVICE_UNAVAILABLE));

        DefendantAccountAtAGlanceResponse out = legacyDefendantAccountService.getAtAGlance(456L);
        assertNotNull(out);
        assertEquals("456", out.getDefendantAccountId());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAtAGlance_error5xx_noEntity_returnsNull() {
        ParameterizedTypeReference<LegacyGetDefendantAccountAtAGlanceResponse> typeRef =
            new ParameterizedTypeReference<>() {};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(null);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>("<error/>", HttpStatus.INTERNAL_SERVER_ERROR));

        DefendantAccountAtAGlanceResponse out = legacyDefendantAccountService.getAtAGlance(999L);
        assertNull(out);
    }

    @Test
    void getAtAGlance_gatewayThrows_hitsCatchAndRethrows() {
        doThrow(new RuntimeException("boom"))
            .when(gatewayService)
            .postToGateway(any(), any(), any(), any());

        assertThrows(RuntimeException.class, () -> legacyDefendantAccountService.getAtAGlance(1L));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAtAGlance_mapsOrganisationBranch_withAliases() {
        OrganisationDetails.OrganisationAlias orgAlias = OrganisationDetails.OrganisationAlias.builder()
            .aliasId("10")
            .sequenceNumber(Short.valueOf("2"))
            .organisationName("Alt Name Ltd")
            .build();

        OrganisationDetails legacyOrg = OrganisationDetails.builder()
            .organisationName("Acme Ltd")
            .organisationAliases(new OrganisationDetails.OrganisationAlias[] {orgAlias})
            .build();

        LegacyPartyDetails party = LegacyPartyDetails.builder()
            .organisationFlag(Boolean.TRUE)
            .partyId("777")
            .organisationDetails(legacyOrg)
            .build();

        LegacyGetDefendantAccountAtAGlanceResponse body =
            LegacyGetDefendantAccountAtAGlanceResponse.builder()
                .partyDetails(party)
                .version(0L)
                .build();

        ParameterizedTypeReference<LegacyGetDefendantAccountAtAGlanceResponse> typeRef =
            new ParameterizedTypeReference<>() {};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(body);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(body.toXml(), HttpStatus.OK));

        DefendantAccountAtAGlanceResponse out = legacyDefendantAccountService.getAtAGlance(1L);

        PartyDetails pd = out.getPartyDetails();
        assertNotNull(pd);
        assertEquals(true, pd.getOrganisationFlag());
        assertEquals("777", pd.getPartyId());
        assertNotNull(pd.getOrganisationDetails());
        assertNull(pd.getIndividualDetails());
        assertEquals("Acme Ltd", pd.getOrganisationDetails().getOrganisationName());
        assertNotNull(pd.getOrganisationDetails().getOrganisationAliases());
        assertEquals(1, pd.getOrganisationDetails().getOrganisationAliases().size());
        assertEquals("10", pd.getOrganisationDetails().getOrganisationAliases().get(0).getAliasId());
        assertEquals(2, pd.getOrganisationDetails().getOrganisationAliases().get(0).getSequenceNumber());
        assertEquals("Alt Name Ltd", pd.getOrganisationDetails().getOrganisationAliases().get(0).getOrganisationName());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAtAGlance_mapsIndividualBranch_withAliases_andDobFormatting() {
        IndividualDetails.IndividualAlias alias = IndividualDetails.IndividualAlias.builder()
            .aliasId("21")
            .sequenceNumber(Short.valueOf("3"))
            .surname("Smith")
            .forenames("John")
            .build();

        IndividualDetails ind = IndividualDetails.builder()
            .title("Mr")
            .firstNames("John James")
            .surname("Smith")
            .dateOfBirth(null)
            .age("34")
            .nationalInsuranceNumber("QQ123456C")
            .individualAliases(new IndividualDetails.IndividualAlias[] {alias})
            .build();

        LegacyPartyDetails party = LegacyPartyDetails.builder()
            .organisationFlag(Boolean.FALSE)
            .partyId("1001")
            .individualDetails(ind)
            .build();

        LegacyGetDefendantAccountAtAGlanceResponse body =
            LegacyGetDefendantAccountAtAGlanceResponse.builder()
                .partyDetails(party)
                .version(0L)
                .build();

        ParameterizedTypeReference<LegacyGetDefendantAccountAtAGlanceResponse> typeRef =
            new ParameterizedTypeReference<>() {};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(body);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(body.toXml(), HttpStatus.OK));

        DefendantAccountAtAGlanceResponse out = legacyDefendantAccountService.getAtAGlance(1L);

        PartyDetails pd = out.getPartyDetails();
        assertEquals(false, pd.getOrganisationFlag());
        assertEquals("1001", pd.getPartyId());
        assertNull(pd.getOrganisationDetails());
        assertNotNull(pd.getIndividualDetails());
        assertEquals("Mr", pd.getIndividualDetails().getTitle());
        assertEquals("John James", pd.getIndividualDetails().getForenames());
        assertEquals("Smith", pd.getIndividualDetails().getSurname());
        assertNull(pd.getIndividualDetails().getDateOfBirth());
        assertEquals("34", pd.getIndividualDetails().getAge());
        assertEquals("QQ123456C", pd.getIndividualDetails().getNationalInsuranceNumber());
        assertNotNull(pd.getIndividualDetails().getIndividualAliases());
        assertEquals(1, pd.getIndividualDetails().getIndividualAliases().size());
        assertEquals("21", pd.getIndividualDetails().getIndividualAliases().get(0).getAliasId());
        assertEquals(3, pd.getIndividualDetails().getIndividualAliases().get(0).getSequenceNumber());
        assertEquals("Smith", pd.getIndividualDetails().getIndividualAliases().get(0).getSurname());
        assertEquals("John", pd.getIndividualDetails().getIndividualAliases().get(0).getForenames());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAtAGlance_mapsAddress_language_payment_enforcement_comments() {
        AddressDetailsLegacy addr =
            AddressDetailsLegacy.builder()
                .addressLine1("1 Street")
                .addressLine2("Area")
                .addressLine3(null)
                .addressLine4("Town")
                .addressLine5("County")
                .postcode("AB1 2CD")
                .build();

        LanguagePreferences.DocumentLanguagePreference dlp =
            LanguagePreferences.DocumentLanguagePreference.builder().documentLanguageCode("EN").build();
        LanguagePreferences.HearingLanguagePreference hlp =
            LanguagePreferences.HearingLanguagePreference.builder().hearingLanguageCode("CY").build();
        LanguagePreferences legacyLang = LanguagePreferences.builder()
            .documentLanguagePreference(dlp)
            .hearingLanguagePreference(hlp)
            .build();

        PaymentTermsSummary legacyPts = PaymentTermsSummary.builder()
            .paymentTermsType(new LegacyPaymentTermsType(LegacyPaymentTermsType.PaymentTermsTypeCode.I))
            .instalmentPeriod(new LegacyInstalmentPeriod(LegacyInstalmentPeriod.InstalmentPeriodCode.W))
            .effectiveDate(LocalDate.of(2024, 1, 2))
            .lumpSumAmount(new BigDecimal("250.00"))
            .instalmentAmount(new BigDecimal("25.00"))
            .build();

        LastEnforcementAction lea = LastEnforcementAction.builder()
            .lastEnforcementActionId("REM")
            .lastEnforcementActionTitle("Reminder")
            .build();

        EnforcementStatusSummary legacyEnf = EnforcementStatusSummary.builder()
            .lastEnforcementAction(lea)
            .collectionOrderMade(Boolean.TRUE)
            .defaultDaysInJail(7)
            .enforcementOverride(null)
            .lastMovementDate(LocalDate.of(2023, 12, 31))
            .build();

        CommentsAndNotes legacyCom = CommentsAndNotes.builder()
            .accountComment("Main note")
            .freeTextNote1("N1")
            .freeTextNote2("N2")
            .freeTextNote3("N3")
            .build();

        LegacyGetDefendantAccountAtAGlanceResponse body =
            LegacyGetDefendantAccountAtAGlanceResponse.builder()
                .address(addr)
                .languagePreferences(legacyLang)
                .paymentTermsSummary(legacyPts)
                .enforcementStatusSummary(legacyEnf)
                .commentsAndNotes(legacyCom)
                .version(0L)
                .build();

        ParameterizedTypeReference<LegacyGetDefendantAccountAtAGlanceResponse> typeRef =
            new ParameterizedTypeReference<>() {};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(body);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(body.toXml(), HttpStatus.OK));

        DefendantAccountAtAGlanceResponse out = legacyDefendantAccountService.getAtAGlance(1L);

        assertNotNull(out.getAddressDetails());
        assertEquals("1 Street", out.getAddressDetails().getAddressLine1());
        assertNull(out.getAddressDetails().getAddressLine3());
        assertEquals("AB1 2CD", out.getAddressDetails().getPostcode());

        assertNotNull(out.getLanguagePreferences());

        uk.gov.hmcts.opal.dto.common.PaymentTermsSummary pts = out.getPaymentTermsSummary();
        assertEquals(LocalDate.of(2024, 1, 2), pts.getEffectiveDate());
        assertEquals(new BigDecimal("250.00"), pts.getLumpSumAmount());
        assertEquals(new BigDecimal("25.00"), pts.getInstalmentAmount());

        uk.gov.hmcts.opal.dto.common.EnforcementStatusSummary es = out.getEnforcementStatus();
        assertNotNull(es);
        assertNotNull(es.getLastEnforcementAction());
        assertEquals("REM", es.getLastEnforcementAction().getLastEnforcementActionId());
        assertEquals("Reminder", es.getLastEnforcementAction().getLastEnforcementActionTitle());
        assertEquals(Boolean.TRUE, es.getCollectionOrderMade());
        assertEquals(7, es.getDefaultDaysInJail());
        assertNull(es.getEnforcementOverride());
        assertEquals(LocalDate.of(2023, 12, 31), es.getLastMovementDate());

        uk.gov.hmcts.opal.dto.common.CommentsAndNotes cn = out.getCommentsAndNotes();
        assertEquals("Main note", cn.getAccountNotesAccountComments());
        assertEquals("N1", cn.getAccountNotesFreeTextNote1());
        assertEquals("N2", cn.getAccountNotesFreeTextNote2());
        assertEquals("N3", cn.getAccountNotesFreeTextNote3());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAtAGlance_paymentTerms_nullEnumsHandled() {
        PaymentTermsSummary legacyPtsNulls =
            PaymentTermsSummary.builder()
                .paymentTermsType(null)
                .instalmentPeriod(null)
                .build();

        LegacyGetDefendantAccountAtAGlanceResponse body =
            LegacyGetDefendantAccountAtAGlanceResponse.builder()
                .paymentTermsSummary(legacyPtsNulls)
                .version(0L)
                .build();

        ParameterizedTypeReference<LegacyGetDefendantAccountAtAGlanceResponse> typeRef =
            new ParameterizedTypeReference<>() {};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(body);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(body.toXml(), HttpStatus.OK));

        DefendantAccountAtAGlanceResponse out = legacyDefendantAccountService.getAtAGlance(1L);
        assertNotNull(out.getPaymentTermsSummary());
        assertNull(out.getPaymentTermsSummary().getPaymentTermsType());
        assertNull(out.getPaymentTermsSummary().getInstalmentPeriod());
    }
}
