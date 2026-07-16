package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.common.InstalmentPeriod;
import uk.gov.hmcts.opal.dto.common.PaymentTermsType;
import uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTerms;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType;
import uk.gov.hmcts.opal.dto.legacy.LegacyPostedDetails;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountPaymentTermsResponse;

class LegacyDefAccServicePaymentTermsTest extends AbstractLegacyDefAccServiceTest {

    @SuppressWarnings("unchecked")
    @Test
    void getPaymentTerms_success_spyGatewayAndRestClientStub() {
        LegacyGetDefendantAccountPaymentTermsResponse responseBody =
            LegacyGetDefendantAccountPaymentTermsResponse.builder()
                .version(2L)
                .paymentTerms(
                    LegacyPaymentTerms.builder()
                        .daysInDefault(120)
                        .extension(false)
                        .paymentTermsType(new LegacyPaymentTermsType(LegacyPaymentTermsType.PaymentTermsTypeCode.B))
                        .instalmentPeriod(new LegacyInstalmentPeriod(LegacyInstalmentPeriod.InstalmentPeriodCode.W))
                        .postedDetails(new LegacyPostedDetails(LocalDateTime.of(2023, 11, 3, 11, 15, 12),
                            "01000000A", ""))
                        .build()
                )
                .paymentCardLastRequested(LocalDate.of(2024, 1, 1))
                .lastEnforcement("REM")
                .build();

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountPaymentTermsResponse>>any()
        )).thenReturn(responseBody);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(responseBody.toXml(), HttpStatus.OK));

        GetDefendantAccountPaymentTermsResponse out = legacyDefendantAccountPaymentTermsService.getPaymentTerms(99L);

        assertNotNull(out);
        assertEquals(BigInteger.valueOf(2), out.getVersion());
        assertEquals(120, out.getPaymentTerms().getDaysInDefault());
        assertEquals(InstalmentPeriod.InstalmentPeriodCode.W,
            out.getPaymentTerms().getInstalmentPeriod().getInstalmentPeriodCode());
        assertEquals(PaymentTermsType.PaymentTermsTypeCode.B,
            out.getPaymentTerms().getPaymentTermsType().getPaymentTermsTypeCode());
        assertEquals("REM", out.getLastEnforcement());
    }

    @SuppressWarnings("unchecked")
    @Test
    void getPaymentTerms_legacyFailure5xx_withEntity_mapsAnyway() {
        LegacyGetDefendantAccountPaymentTermsResponse responseBody =
            LegacyGetDefendantAccountPaymentTermsResponse.builder()
                .version(3L)
                .paymentTerms(
                    LegacyPaymentTerms.builder()
                        .daysInDefault(5)
                        .paymentTermsType(new LegacyPaymentTermsType(LegacyPaymentTermsType.PaymentTermsTypeCode.P))
                        .instalmentPeriod(new LegacyInstalmentPeriod(LegacyInstalmentPeriod.InstalmentPeriodCode.M))
                        .build()
                )
                .build();

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountPaymentTermsResponse>>any()
        )).thenReturn(responseBody);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(responseBody.toXml(), HttpStatus.SERVICE_UNAVAILABLE));

        GetDefendantAccountPaymentTermsResponse out = legacyDefendantAccountPaymentTermsService.getPaymentTerms(1L);

        assertNotNull(out);
        assertEquals(BigInteger.valueOf(3L), out.getVersion());
        assertEquals(5, out.getPaymentTerms().getDaysInDefault());
        assertEquals(PaymentTermsType.PaymentTermsTypeCode.P,
            out.getPaymentTerms().getPaymentTermsType().getPaymentTermsTypeCode());
        assertEquals(InstalmentPeriod.InstalmentPeriodCode.M,
            out.getPaymentTerms().getInstalmentPeriod().getInstalmentPeriodCode());
    }

    @SuppressWarnings("unchecked")
    @Test
    void getPaymentTerms_error5xx_returnsNull() {
        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountPaymentTermsResponse>>any()
        )).thenReturn(null);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>("<error/>", HttpStatus.INTERNAL_SERVER_ERROR));

        GetDefendantAccountPaymentTermsResponse out = legacyDefendantAccountPaymentTermsService.getPaymentTerms(42L);

        assertNull(out);
    }

    @SuppressWarnings("unchecked")
    @Test
    void getPaymentTerms_success_withNullEntity_returnsEmptyDto() {
        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountPaymentTermsResponse>>any()
        )).thenReturn(null);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>("<response/>", HttpStatus.OK));

        GetDefendantAccountPaymentTermsResponse out = legacyDefendantAccountPaymentTermsService.getPaymentTerms(3L);

        assertNotNull(out);
        assertEquals(BigInteger.valueOf(1L), out.getVersion());
        assertNull(out.getPaymentTerms());
        assertNull(out.getPaymentCardLastRequested());
        assertNull(out.getLastEnforcement());
    }

    @SuppressWarnings("unchecked")
    @Test
    void getPaymentTerms_mapsNullNestedObjects_toNulls() {
        LegacyPaymentTerms legacyTerms =
            LegacyPaymentTerms.builder()
                .daysInDefault(0)
                .dateDaysInDefaultImposed(null)
                .reasonForExtension(null)
                .paymentTermsType(null)
                .effectiveDate(null)
                .instalmentPeriod(null)
                .lumpSumAmount(null)
                .instalmentAmount(null)
                .postedDetails(null)
                .build();

        LegacyGetDefendantAccountPaymentTermsResponse responseBody =
            LegacyGetDefendantAccountPaymentTermsResponse.builder()
                .version(4L)
                .paymentTerms(legacyTerms)
                .build();

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountPaymentTermsResponse>>any()
        )).thenReturn(responseBody);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(responseBody.toXml(), HttpStatus.OK));

        GetDefendantAccountPaymentTermsResponse out = legacyDefendantAccountPaymentTermsService.getPaymentTerms(4L);

        assertNotNull(out);
        assertNotNull(out.getPaymentTerms());
        assertNull(out.getPaymentTerms().getPaymentTermsType());
        assertNull(out.getPaymentTerms().getInstalmentPeriod());
        assertNull(out.getPaymentTerms().getPostedDetails());
    }

    @Test
    void legacyPaymentTerms_nonNullEnums_areConverted() {
        LegacyGetDefendantAccountPaymentTermsResponse legacy = LegacyGetDefendantAccountPaymentTermsResponse.builder()
            .version(1L)
            .paymentTerms(
                LegacyPaymentTerms.builder()
                    .paymentTermsType(new LegacyPaymentTermsType(LegacyPaymentTermsType.PaymentTermsTypeCode.B))
                    .instalmentPeriod(new LegacyInstalmentPeriod(LegacyInstalmentPeriod.InstalmentPeriodCode.W))
                    .build()
            )
            .build();

        doReturn(new GatewayService.Response<>(HttpStatus.OK, legacy, null, null))
            .when(gatewayService).postToGateway(eq(LegacyDefendantAccountService.GET_PAYMENT_TERMS),
                eq(LegacyGetDefendantAccountPaymentTermsResponse.class), any(), any());

        GetDefendantAccountPaymentTermsResponse out = legacyDefendantAccountPaymentTermsService.getPaymentTerms(123L);

        assertNotNull(out.getPaymentTerms().getPaymentTermsType());
        assertNotNull(out.getPaymentTerms().getInstalmentPeriod());
    }
}
