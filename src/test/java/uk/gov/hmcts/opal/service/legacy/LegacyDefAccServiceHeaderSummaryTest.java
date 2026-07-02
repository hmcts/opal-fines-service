package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.common.PaymentStateSummary;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.generated.model.AccountStatusReferenceCommon;
import uk.gov.hmcts.opal.generated.model.AccountStatusReferenceCommon.AccountStatusCodeEnum;
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryCommon;
import uk.gov.hmcts.opal.generated.model.GetDefendantAccountHeaderSummary200Response;
import uk.gov.hmcts.opal.generated.model.GetDefendantAccountHeaderSummary200Response.AccountTypeEnum;
import uk.gov.hmcts.opal.generated.model.GetDefendantAccountHeaderSummary200Response.DebtorTypeEnum;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PaymentStateSummaryCommon;

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

        final GetDefendantAccountHeaderSummary200Response expected =
            GetDefendantAccountHeaderSummary200Response.builder()
            .defendantAccountId("1")
            .debtorType(DebtorTypeEnum.DEFENDANT)
            .isYouth(false)
            .accountNumber("SAMPLE")
            .accountType(AccountTypeEnum.FINE)
            .accountStatusReference(AccountStatusReferenceCommon.builder()
                                        .accountStatusCode(AccountStatusCodeEnum.L)
                                        .accountStatusDisplayName(null)
                                        .build())
            .businessUnitSummary(BusinessUnitSummaryCommon.builder()
                                     .businessUnitId("1")
                                     .businessUnitName("Test BU")
                                     .welshSpeaking("N")
                                     .build())
            .paymentStateSummary(PaymentStateSummaryCommon.builder()
                                     .imposedAmount(BigDecimal.ZERO)
                                     .arrearsAmount(BigDecimal.ZERO)
                                     .paidAmount(BigDecimal.ZERO)
                                     .accountBalance(BigDecimal.ZERO)
                                     .build())
            .partyDetails(PartyDetailsCommon.builder()
                              .partyId("1")
                              .organisationFlag(false)
                              .organisationDetails(null)
                              .individualDetails(null)
                              .build())
            .build();

        assertNotNull(actual, "Expected non-null header summary");
        assertEquals(expected.getDefendantAccountId(), actual.getResponse().getDefendantAccountId());
        assertEquals(expected.getDebtorType(), actual.getResponse().getDebtorType());
        assertEquals(expected.getIsYouth(), actual.getResponse().getIsYouth());
        assertEquals(expected.getAccountNumber(), actual.getResponse().getAccountNumber());
        assertEquals(expected.getAccountStatusReference().getAccountStatusCode(),
            actual.getResponse().getAccountStatusReference().getAccountStatusCode());
        assertEquals(expected.getBusinessUnitSummary().getBusinessUnitName(),
            actual.getResponse().getBusinessUnitSummary().getBusinessUnitName());
        assertEquals(expected.getPaymentStateSummary().getImposedAmount(),
            actual.getResponse().getPaymentStateSummary().getImposedAmount());
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
        assertEquals("SAMPLE", published.getResponse().getAccountNumber());
        assertEquals(AccountTypeEnum.FINE, published.getResponse().getAccountType());
        assertEquals(AccountStatusCodeEnum.L,
            published.getResponse().getAccountStatusReference().getAccountStatusCode());
        assertEquals("Live", published.getResponse().getAccountStatusReference().getAccountStatusDisplayName());
        assertEquals("78", published.getResponse().getBusinessUnitSummary().getBusinessUnitId());
        assertEquals("Test BU", published.getResponse().getBusinessUnitSummary().getBusinessUnitName());
        assertEquals(new BigDecimal("700.58"), published.getResponse().getPaymentStateSummary().getImposedAmount());
        assertEquals(BigDecimal.ZERO, published.getResponse().getPaymentStateSummary().getArrearsAmount());
        assertEquals(new BigDecimal("200.00"), published.getResponse().getPaymentStateSummary().getPaidAmount());
        assertEquals(new BigDecimal("500.58"), published.getResponse().getPaymentStateSummary().getAccountBalance());
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
        assertEquals("77", result.getResponse().getDefendantAccountPartyId());
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
        PaymentStateSummaryCommon paymentStateSummary = published.getResponse().getPaymentStateSummary();
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

        assertEquals("SAMPLE", published.getResponse().getAccountNumber());
        assertEquals(AccountTypeEnum.FINE, published.getResponse().getAccountType());
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

        assertEquals("SAMPLE", published.getResponse().getAccountNumber());
        assertEquals(AccountTypeEnum.FINE, published.getResponse().getAccountType());
        assertNull(published.getResponse().getAccountStatusReference());
        assertNull(published.getResponse().getBusinessUnitSummary());
        assertNull(published.getResponse().getPaymentStateSummary());
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

        assertEquals("SAMPLE", published.getResponse().getAccountNumber());
        assertEquals(AccountTypeEnum.FINE, published.getResponse().getAccountType());
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
        assertEquals("SAMPLE", out.getResponse().getAccountNumber());
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

        PaymentStateSummaryCommon ps = out.getResponse().getPaymentStateSummary();
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
        assertEquals(BigDecimal.ZERO, out.getResponse().getPaymentStateSummary().getImposedAmount());
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
        assertNotNull(out.getResponse().getPartyDetails().getIndividualDetails().getIndividualAliases());
        assertEquals(1, out.getResponse().getPartyDetails().getIndividualDetails().getIndividualAliases().size());
    }

    @Test
    void getHeaderSummary_returns_true_for_hasConsolidatedAccounts() {
        LegacyGetDefendantAccountHeaderSummaryResponse resp = createHeaderSummaryResponse();
        resp.setHasConsolidatedAccounts(Boolean.TRUE);

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountHeaderSummaryResponse>>any()
        )).thenReturn(resp);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(resp.toXml(), HttpStatus.OK));

        DefendantAccountHeaderSummary out = legacyDefendantAccountService.getHeaderSummary(1L);
        assertTrue(out.getResponse().getHasConsolidatedAccounts());
    }

    @Test
    void getHeaderSummary_returns_false_for_hasConsolidatedAccounts() {
        LegacyGetDefendantAccountHeaderSummaryResponse resp = createHeaderSummaryResponse();
        resp.setHasConsolidatedAccounts(Boolean.FALSE);

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountHeaderSummaryResponse>>any()
        )).thenReturn(resp);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(resp.toXml(), HttpStatus.OK));

        DefendantAccountHeaderSummary out = legacyDefendantAccountService.getHeaderSummary(1L);
        assertFalse(out.getResponse().getHasConsolidatedAccounts());
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
