package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.common.AccountStatusReference;
import uk.gov.hmcts.opal.dto.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.common.PaymentStateSummary;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHeaderSummaryResponse;

class LegacyDefAccServiceHeaderSummaryTest extends AbstractLegacyDefAccServiceTest {

    @SuppressWarnings("unchecked")
    @Test
    void testGetHeaderSummary_success() {
        LegacyGetDefendantAccountHeaderSummaryResponse responseBody = createHeaderSummaryResponse();

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountHeaderSummaryResponse>>any()
        )).thenReturn(responseBody);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(responseBody.toXml(), HttpStatus.OK));

        DefendantAccountHeaderSummary actual = legacyDefendantAccountService.getHeaderSummary(1L);

        final DefendantAccountHeaderSummary expected = DefendantAccountHeaderSummary.builder()
            .version(BigInteger.valueOf(1L))
            .defendantAccountId("1")
            .debtorType("Defendant")
            .isYouth(false)
            .accountNumber("SAMPLE")
            .accountType("Fine")
            .accountStatusReference(AccountStatusReference.builder()
                                        .accountStatusCode("L")
                                        .accountStatusDisplayName(null)
                                        .build())
            .businessUnitSummary(BusinessUnitSummary.builder()
                                     .businessUnitId("1")
                                     .businessUnitName("Test BU")
                                     .welshSpeaking("N")
                                     .build())
            .paymentStateSummary(PaymentStateSummary.builder()
                                     .imposedAmount(BigDecimal.ZERO)
                                     .arrearsAmount(BigDecimal.ZERO)
                                     .paidAmount(BigDecimal.ZERO)
                                     .accountBalance(BigDecimal.ZERO)
                                     .build())
            .partyDetails(PartyDetails.builder()
                              .partyId("1")
                              .organisationFlag(false)
                              .organisationDetails(null)
                              .individualDetails(null)
                              .build())
            .build();

        assertNotNull(actual, "Expected non-null header summary");
        assertEquals(expected.getDefendantAccountId(), actual.getDefendantAccountId());
        assertEquals(expected.getDebtorType(), actual.getDebtorType());
        assertEquals(expected.getIsYouth(), actual.getIsYouth());
        assertEquals(expected.getAccountNumber(), actual.getAccountNumber());
        assertEquals(expected.getAccountStatusReference().getAccountStatusCode(),
            actual.getAccountStatusReference().getAccountStatusCode());
        assertEquals(expected.getBusinessUnitSummary().getBusinessUnitName(),
            actual.getBusinessUnitSummary().getBusinessUnitName());
        assertEquals(expected.getPaymentStateSummary().getImposedAmount(),
            actual.getPaymentStateSummary().getImposedAmount());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetHeaderSummary_nonZeroAmounts_andCustomBu() {
        LegacyGetDefendantAccountHeaderSummaryResponse responseBody =
            LegacyGetDefendantAccountHeaderSummaryResponse.builder()
                .version("1")
                .defendantAccountId("1")
                .accountNumber("SAMPLE")
                .accountType("Fine")
                .accountStatusReference(
                    uk.gov.hmcts.opal.dto.legacy.common.AccountStatusReference.builder()
                        .accountStatusCode("L")
                        .build()
                )
                .businessUnitSummary(
                    uk.gov.hmcts.opal.dto.legacy.common.BusinessUnitSummary.builder()
                        .businessUnitId("78")
                        .businessUnitName("Test BU")
                        .welshSpeaking("N")
                        .build()
                )
                .paymentStateSummary(
                    uk.gov.hmcts.opal.dto.legacy.common.PaymentStateSummary.builder()
                        .imposedAmount("700.58")
                        .arrearsAmount("0")
                        .paidAmount("200.00")
                        .accountBalance("500.58")
                        .build()
                )
                .partyDetails(
                    uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails.builder()
                        .organisationFlag(false)
                        .build()
                )
                .build();

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountHeaderSummaryResponse>>any()
        )).thenReturn(responseBody);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(responseBody.toXml(), HttpStatus.OK));

        DefendantAccountHeaderSummary published = legacyDefendantAccountService.getHeaderSummary(1L);

        assertNotNull(published);
        assertEquals("SAMPLE", published.getAccountNumber());
        assertEquals("Fine", published.getAccountType());
        assertEquals("L", published.getAccountStatusReference().getAccountStatusCode());
        assertEquals("Live", published.getAccountStatusReference().getAccountStatusDisplayName());
        assertEquals("78", published.getBusinessUnitSummary().getBusinessUnitId());
        assertEquals("Test BU", published.getBusinessUnitSummary().getBusinessUnitName());
        assertEquals(new BigDecimal("700.58"), published.getPaymentStateSummary().getImposedAmount());
        assertEquals(BigDecimal.ZERO, published.getPaymentStateSummary().getArrearsAmount());
        assertEquals(new BigDecimal("200.00"), published.getPaymentStateSummary().getPaidAmount());
        assertEquals(new BigDecimal("500.58"), published.getPaymentStateSummary().getAccountBalance());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetHeaderSummary_setsDefendantPartyIdCorrectly() {
        LegacyGetDefendantAccountHeaderSummaryResponse responseBody = createHeaderSummaryResponse();
        responseBody.setDefendantPartyId("77");

        when(restClient.responseSpec.body(Mockito.<ParameterizedTypeReference
            <LegacyGetDefendantAccountHeaderSummaryResponse>>any()))
            .thenReturn(responseBody);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(responseBody.toXml(), HttpStatus.OK));

        DefendantAccountHeaderSummary result = legacyDefendantAccountService.getHeaderSummary(1L);

        assertNotNull(result);
        assertEquals("77", result.getDefendantAccountPartyId());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetHeaderSummary_blankAmounts_defaultToZero() {
        LegacyGetDefendantAccountHeaderSummaryResponse responseBody =
            LegacyGetDefendantAccountHeaderSummaryResponse.builder()
                .defendantAccountId("1")
                .accountNumber("SAMPLE")
                .accountType("Fine")
                .accountStatusReference(
                    uk.gov.hmcts.opal.dto.legacy.common.AccountStatusReference.builder()
                        .accountStatusCode("L")
                        .accountStatusDisplayName("Live")
                        .build()
                )
                .businessUnitSummary(
                    uk.gov.hmcts.opal.dto.legacy.common.BusinessUnitSummary.builder()
                        .businessUnitId("78")
                        .businessUnitName("Test BU")
                        .welshSpeaking("N")
                        .build()
                )
                .paymentStateSummary(
                    uk.gov.hmcts.opal.dto.legacy.common.PaymentStateSummary.builder()
                        .imposedAmount("")
                        .arrearsAmount(null)
                        .paidAmount("NaN")
                        .accountBalance("0")
                        .build()
                )
                .partyDetails(uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails.builder().build())
                .build();

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountHeaderSummaryResponse>>any()
        )).thenReturn(responseBody);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(responseBody.toXml(), HttpStatus.OK));

        DefendantAccountHeaderSummary published = legacyDefendantAccountService.getHeaderSummary(1L);
        PaymentStateSummary paymentStateSummary = published.getPaymentStateSummary();
        assertEquals(BigDecimal.ZERO, paymentStateSummary.getImposedAmount());
        assertEquals(BigDecimal.ZERO, paymentStateSummary.getArrearsAmount());
        assertEquals(BigDecimal.ZERO, paymentStateSummary.getPaidAmount());
        assertEquals(BigDecimal.ZERO, paymentStateSummary.getAccountBalance());
    }

    @Test
    void testGetHeaderSummary_gatewayThrows_hitsCatchAndRethrows() {
        doThrow(new RuntimeException("boom"))
            .when(gatewayService)
            .postToGateway(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

        assertThrows(RuntimeException.class, () -> legacyDefendantAccountService.getHeaderSummary(1L));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetHeaderSummary_withIndividualDetails_executesMappingBranches() {
        uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails legacyInd =
            uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails.builder()
                .title("Mr")
                .firstNames("John")
                .surname("Smith")
                .individualAliases(new uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails.IndividualAlias[0])
                .build();

        uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails party =
            uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails.builder()
                .individualDetails(legacyInd)
                .build();

        LegacyGetDefendantAccountHeaderSummaryResponse responseBody = createHeaderSummaryResponse();
        responseBody.setPartyDetails(party);

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountHeaderSummaryResponse>>any()
        )).thenReturn(responseBody);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(responseBody.toXml(), HttpStatus.OK));

        DefendantAccountHeaderSummary published = legacyDefendantAccountService.getHeaderSummary(1L);

        assertEquals("SAMPLE", published.getAccountNumber());
        assertEquals("Fine", published.getAccountType());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetHeaderSummary_whenCommonSectionsNull_executesNullBranches() {
        LegacyGetDefendantAccountHeaderSummaryResponse responseBody =
            LegacyGetDefendantAccountHeaderSummaryResponse.builder()
                .defendantAccountId("1")
                .accountNumber("SAMPLE")
                .accountType("Fine")
                .partyDetails(uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails.builder().build())
                .build();

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountHeaderSummaryResponse>>any()
        )).thenReturn(responseBody);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(responseBody.toXml(), HttpStatus.OK));

        DefendantAccountHeaderSummary published = legacyDefendantAccountService.getHeaderSummary(1L);

        assertEquals("SAMPLE", published.getAccountNumber());
        assertEquals("Fine", published.getAccountType());
        assertNull(published.getAccountStatusReference());
        assertNull(published.getBusinessUnitSummary());
        assertNull(published.getPaymentStateSummary());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetHeaderSummary_withOrganisationDetails_executesMappingBranches() {
        uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails.OrganisationAlias[] orgAliasArr =
            new uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails.OrganisationAlias[] {
                uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails.OrganisationAlias.builder()
                    .aliasId("ORG1")
                    .sequenceNumber(Short.valueOf("1"))
                    .organisationName("AliasCo")
                    .build()
            };

        uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails legacyOrg =
            uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails.builder()
                .organisationName("MainCo")
                .organisationAliases(orgAliasArr)
                .build();

        uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails party =
            uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails.builder()
                .organisationDetails(legacyOrg)
                .build();

        LegacyGetDefendantAccountHeaderSummaryResponse responseBody = createHeaderSummaryResponse();
        responseBody.setPartyDetails(party);

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountHeaderSummaryResponse>>any()
        )).thenReturn(responseBody);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(responseBody.toXml(), HttpStatus.OK));

        DefendantAccountHeaderSummary published = legacyDefendantAccountService.getHeaderSummary(1L);

        assertEquals("SAMPLE", published.getAccountNumber());
        assertEquals("Fine", published.getAccountType());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetHeaderSummary_legacyFailure5xx_logsAndMaps() {
        LegacyGetDefendantAccountHeaderSummaryResponse responseBody = createHeaderSummaryResponse();

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountHeaderSummaryResponse>>any()
        )).thenReturn(responseBody);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(responseBody.toXml(), HttpStatus.SERVICE_UNAVAILABLE));

        DefendantAccountHeaderSummary out = legacyDefendantAccountService.getHeaderSummary(1L);
        assertNotNull(out);
        assertEquals("SAMPLE", out.getAccountNumber());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetHeaderSummary_exceptionBranch_rethrows() {
        LegacyGetDefendantAccountHeaderSummaryResponse responseBody = createHeaderSummaryResponse();

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountHeaderSummaryResponse>>any()
        )).thenReturn(responseBody);
        when(restClient.responseSpec.toEntity(String.class))
            .thenThrow(new RuntimeException("boom"));

        assertThrows(RuntimeException.class, () -> legacyDefendantAccountService.getHeaderSummary(1L));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetHeaderSummary_numberInputs_mapToBigDecimal() {
        LegacyGetDefendantAccountHeaderSummaryResponse resp = createHeaderSummaryResponse();
        PaymentStateSummary pay = PaymentStateSummary.builder()
            .imposedAmount(new BigDecimal("100.0"))
            .arrearsAmount(BigDecimal.valueOf(0))
            .paidAmount(BigDecimal.valueOf(25L))
            .accountBalance(BigDecimal.valueOf(75.5f))
            .build();

        resp.setPaymentStateSummary(
            uk.gov.hmcts.opal.dto.legacy.common.PaymentStateSummary.builder()
                .imposedAmount(pay.getImposedAmount().toString())
                .arrearsAmount(pay.getArrearsAmount().toString())
                .paidAmount(pay.getPaidAmount().toString())
                .accountBalance(pay.getAccountBalance().toString())
                .build()
        );

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountHeaderSummaryResponse>>any()
        )).thenReturn(resp);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(resp.toXml(), HttpStatus.OK));

        DefendantAccountHeaderSummary out = legacyDefendantAccountService.getHeaderSummary(1L);

        PaymentStateSummary ps = out.getPaymentStateSummary();
        assertEquals(0, ps.getImposedAmount().compareTo(new BigDecimal("100.0")));
        assertEquals(0, ps.getArrearsAmount().compareTo(new BigDecimal("0")));
        assertEquals(0, ps.getPaidAmount().compareTo(new BigDecimal("25")));
        assertEquals(0, ps.getAccountBalance().compareTo(new BigDecimal("75.5")));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetHeaderSummary_unsupportedPaymentType_defaultsZero() {
        uk.gov.hmcts.opal.dto.legacy.common.PaymentStateSummary pay =
            uk.gov.hmcts.opal.dto.legacy.common.PaymentStateSummary.builder()
            .imposedAmount("0")
            .build();

        LegacyGetDefendantAccountHeaderSummaryResponse resp = createHeaderSummaryResponse();
        resp.setPaymentStateSummary(pay);

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountHeaderSummaryResponse>>any()
        )).thenReturn(resp);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(resp.toXml(), HttpStatus.OK));

        DefendantAccountHeaderSummary out = legacyDefendantAccountService.getHeaderSummary(1L);
        assertEquals(BigDecimal.ZERO, out.getPaymentStateSummary().getImposedAmount());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetHeaderSummary_individualAliases_areMapped() {
        uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails.IndividualAlias alias =
            uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails.IndividualAlias.builder()
            .aliasId("AL1")
            .sequenceNumber(Short.valueOf("1"))
            .surname("AliasSurname")
            .forenames("AliasForenames")
            .build();
        uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails ind =
            uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails.builder()
            .title("Mr")
            .firstNames("John")
            .surname("Smith")
            .individualAliases(new uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails.IndividualAlias[] {alias})
            .build();

        uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails party =
            uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails.builder()
            .individualDetails(ind)
            .build();

        LegacyGetDefendantAccountHeaderSummaryResponse resp = createHeaderSummaryResponse();
        resp.setPartyDetails(party);

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountHeaderSummaryResponse>>any()
        )).thenReturn(resp);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(resp.toXml(), HttpStatus.OK));

        DefendantAccountHeaderSummary out = legacyDefendantAccountService.getHeaderSummary(1L);
        assertNotNull(out.getPartyDetails().getIndividualDetails().getIndividualAliases());
        assertEquals(1, out.getPartyDetails().getIndividualDetails().getIndividualAliases().size());
    }

    private LegacyGetDefendantAccountHeaderSummaryResponse createHeaderSummaryResponse() {
        return LegacyGetDefendantAccountHeaderSummaryResponse.builder()
            .defendantAccountId("1")
            .accountNumber("SAMPLE")
            .accountType("Fine")
            .accountStatusReference(
                uk.gov.hmcts.opal.dto.legacy.common.AccountStatusReference.builder()
                    .accountStatusCode("L")
                    .accountStatusDisplayName("Live")
                    .build()
            )
            .businessUnitSummary(
                uk.gov.hmcts.opal.dto.legacy.common.BusinessUnitSummary.builder()
                    .businessUnitId("1")
                    .businessUnitName("Test BU")
                    .welshSpeaking("N")
                    .build()
            )
            .paymentStateSummary(
                uk.gov.hmcts.opal.dto.legacy.common.PaymentStateSummary.builder()
                    .imposedAmount("0")
                    .arrearsAmount("0")
                    .paidAmount("0")
                    .accountBalance("0")
                    .build()
            )
            .partyDetails(uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails.builder().build())
            .build();
    }
}
