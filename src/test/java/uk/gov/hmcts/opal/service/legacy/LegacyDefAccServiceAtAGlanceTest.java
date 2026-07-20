package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import uk.gov.hmcts.opal.dto.GetDefendantAccountAtAGlanceResponse;
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

        GetDefendantAccountAtAGlanceResponse out = legacyDefendantAccountService.getAtAGlance(123L);

        assertNotNull(out);
        assertEquals("123", out.getPayload().getDefendantAccountId());
        assertEquals("ACC-42", out.getPayload().getAccountNumber());
        assertEquals("Defendant", out.getPayload().getDebtorType().getValue());
        assertEquals(Boolean.FALSE, out.getPayload().getIsYouth());
        assertEquals(BigInteger.valueOf(5L), out.getVersion());
        assertNull(out.getPayload().getPartyDetails());
        assertNull(out.getPayload().getAddress());
        assertFalse(out.getPayload().getLanguagePreferences().isPresent());
        assertNull(out.getPayload().getPaymentTerms());
        assertNull(out.getPayload().getEnforcementStatus());
        assertFalse(out.getPayload().getCommentsAndNotes().isPresent());
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

        GetDefendantAccountAtAGlanceResponse out = legacyDefendantAccountService.getAtAGlance(456L);
        assertNotNull(out);
        assertEquals("456", out.getPayload().getDefendantAccountId());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAtAGlance_error5xx_noEntity_returnsNull() {
        ParameterizedTypeReference<LegacyGetDefendantAccountAtAGlanceResponse> typeRef =
            new ParameterizedTypeReference<>() {};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(null);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>("<error/>", HttpStatus.INTERNAL_SERVER_ERROR));

        GetDefendantAccountAtAGlanceResponse out = legacyDefendantAccountService.getAtAGlance(999L);
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

        GetDefendantAccountAtAGlanceResponse out = legacyDefendantAccountService.getAtAGlance(1L);

        var pd = out.getPayload().getPartyDetails();
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
            .forenames("John James")
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

        GetDefendantAccountAtAGlanceResponse out = legacyDefendantAccountService.getAtAGlance(1L);

        var pd = out.getPayload().getPartyDetails();
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

        GetDefendantAccountAtAGlanceResponse out = legacyDefendantAccountService.getAtAGlance(1L);

        assertNotNull(out.getPayload().getAddress());
        assertEquals("1 Street", out.getPayload().getAddress().getAddressLine1());
        assertNull(out.getPayload().getAddress().getAddressLine3());
        assertEquals("AB1 2CD", out.getPayload().getAddress().getPostcode());

        assertTrue(out.getPayload().getLanguagePreferences().isPresent());

        var pts = out.getPayload().getPaymentTerms();
        assertEquals(LocalDate.of(2024, 1, 2), pts.getEffectiveDate().get());
        assertEquals(new BigDecimal("250.00"), pts.getLumpSumAmount().get());
        assertEquals(new BigDecimal("25.00"), pts.getInstalmentAmount().get());

        var es = out.getPayload().getEnforcementStatus();
        assertNotNull(es);
        assertTrue(es.getLastEnforcementAction().isPresent());
        assertEquals("REM", es.getLastEnforcementAction().get().getLastEnforcementActionId());
        assertEquals("Reminder", es.getLastEnforcementAction().get().getLastEnforcementActionTitle().get());
        assertEquals(Boolean.TRUE, es.getCollectionOrderMade());
        assertEquals(7, es.getDefaultDaysInJail().get());
        assertFalse(es.getEnforcementOverride().isPresent());
        assertEquals(LocalDate.of(2023, 12, 31), es.getLastMovementDate().get());

        var cn = out.getPayload().getCommentsAndNotes().get();
        assertEquals("Main note", cn.getAccountComment());
        assertEquals("N1", cn.getFreeTextNote1());
        assertEquals("N2", cn.getFreeTextNote2());
        assertEquals("N3", cn.getFreeTextNote3());
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

        GetDefendantAccountAtAGlanceResponse out = legacyDefendantAccountService.getAtAGlance(1L);
        assertNotNull(out.getPayload().getPaymentTerms());
        assertNull(out.getPayload().getPaymentTerms().getPaymentTermsType());
        assertFalse(out.getPayload().getPaymentTerms().getInstalmentPeriod().isPresent());
    }
}
