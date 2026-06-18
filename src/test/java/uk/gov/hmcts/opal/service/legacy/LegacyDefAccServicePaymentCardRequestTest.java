package uk.gov.hmcts.opal.service.legacy;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.opal.util.VersionUtils.extractBigInteger;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.dto.AddPaymentCardRequestResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.legacy.AddPaymentCardLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.AddPaymentCardLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.AddPaymentTermsLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.AddPaymentTermsLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTerms;

class LegacyDefAccServicePaymentCardRequestTest extends AbstractLegacyDefAccServiceTest {

    @Test
    void addPaymentCardRequest_legacy_happyPath() {
        AddPaymentCardLegacyResponse legacyResp = new AddPaymentCardLegacyResponse("123", "4");

        GatewayService.Response<AddPaymentCardLegacyResponse> gwResp =
            new GatewayService.Response<>(HttpStatus.OK, legacyResp, null, null);

        doReturn(gwResp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.ADD_PAYMENT_CARD_REQUEST),
            eq(AddPaymentCardLegacyResponse.class),
            any(AddPaymentCardLegacyRequest.class),
            isNull()
        );

        AddPaymentCardRequestResponse out =
            legacyDefendantAccountService.addPaymentCardRequest(123L, "78", null, "Poster Name", "4", "AUTH");

        assertNotNull(out);
        assertEquals(123L, out.getDefendantAccountId());
    }

    @Test
    void addPaymentCardRequest_legacy_buildsCorrectRequest() {
        GatewayService.Response<AddPaymentCardLegacyResponse> gwResp =
            new GatewayService.Response<>(HttpStatus.OK, new AddPaymentCardLegacyResponse("123", "4"), null, null);

        ArgumentCaptor<AddPaymentCardLegacyRequest> captor = ArgumentCaptor.forClass(AddPaymentCardLegacyRequest.class);

        doReturn(gwResp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.ADD_PAYMENT_CARD_REQUEST),
            eq(AddPaymentCardLegacyResponse.class),
            captor.capture(),
            isNull()
        );

        legacyDefendantAccountService.addPaymentCardRequest(123L, "78", null, "Poster Name", "9", "AUTH");

        AddPaymentCardLegacyRequest sent = captor.getValue();
        assertEquals("123", sent.getDefendantAccountId());
        assertEquals("78", sent.getBusinessUnitId());
        assertEquals(String.valueOf(9), sent.getVersion());
        assertNull(sent.getBusinessUnitUserId());
    }

    @Test
    void addPaymentCardRequest_legacy_5xxFailureThrows() {
        GatewayService.Response<AddPaymentCardLegacyResponse> gwResp =
            new GatewayService.Response<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "<error/>", null);

        doReturn(gwResp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.ADD_PAYMENT_CARD_REQUEST),
            eq(AddPaymentCardLegacyResponse.class),
            any(),
            isNull()
        );

        assertThrows(RuntimeException.class, () ->
            legacyDefendantAccountService.addPaymentCardRequest(99L, "78", null, "Poster Name", "1", "AUTH")
        );
    }

    @Test
    void addPaymentCardRequest_legacy_gatewayExceptionThrows() {
        Throwable ex = new RuntimeException("gateway boom");

        GatewayService.Response<AddPaymentCardLegacyResponse> gwResp =
            new GatewayService.Response<>(HttpStatus.BAD_GATEWAY, ex, null);

        doReturn(gwResp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.ADD_PAYMENT_CARD_REQUEST),
            eq(AddPaymentCardLegacyResponse.class),
            any(),
            isNull()
        );

        assertThrows(RuntimeException.class, () ->
            legacyDefendantAccountService.addPaymentCardRequest(88L, "78", null, "Poster Name", "2", "AUTH")
        );
    }

    @Test
    void addPaymentCardRequest_legacy_nullEntityThrows() {
        GatewayService.Response<AddPaymentCardLegacyResponse> gwResp =
            new GatewayService.Response<>(HttpStatus.OK, null, null, null);

        doReturn(gwResp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.ADD_PAYMENT_CARD_REQUEST),
            eq(AddPaymentCardLegacyResponse.class),
            any(),
            isNull()
        );

        assertThrows(RuntimeException.class, () ->
            legacyDefendantAccountService.addPaymentCardRequest(55L, "78", null, "Poster Name", "3", "AUTH")
        );
    }

    @Test
    void addPaymentCardRequest_legacy_invalidIfMatchThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            legacyDefendantAccountService.addPaymentCardRequest(1L, "78", null, "Poster Name", "notANumber", "AUTH")
        );
    }


    @Test
    void addPaymentTerms_whenGatewayResponseWithSuccess_thenReturnMappedResponse() {
        // Given
        long defendantAccountId = 1L;
        String businessUnitId = "BU";
        String businessUnitUserId = "U";
        String ifMatch = "\"1\"";
        var legacyResponse = createAddPaymentTermsLegacyResponse(defendantAccountId, ifMatch);
        var gateWayResponse = new GatewayService.Response<>(HttpStatus.OK, legacyResponse, null, null);

        // When
        doReturn(gateWayResponse).when(gatewayService).postToGateway(any(), any(), any(), any());

        var actualResponse = legacyDefendantAccountService.addPaymentTerms(
            defendantAccountId, businessUnitId,
            businessUnitUserId, "Poster Name", ifMatch,
            "auth", null
        );

        // Then
        var requestCaptor = ArgumentCaptor.forClass(AddPaymentTermsLegacyRequest.class);

        verify(gatewayService, times(1))
            .postToGateway(
                eq(LegacyDefendantAccountService.ADD_PAYMENT_TERMS),
                eq(AddPaymentTermsLegacyResponse.class),
                requestCaptor.capture(),
                isNull()
            );

        assertAddPaymentTermsLegacyRequest(
            requestCaptor.getValue(), defendantAccountId, businessUnitId,
            businessUnitUserId, ifMatch
        );
        assertGetDefendantAccountPaymentTermsResponse(actualResponse, legacyResponse);
    }

    private static AddPaymentTermsLegacyResponse createAddPaymentTermsLegacyResponse(long defendantAccountId,
        String ifMatch) {
        return AddPaymentTermsLegacyResponse.builder()
            .defendantAccountId(String.valueOf(defendantAccountId))
            .version(extractBigInteger(ifMatch))
            .paymentTerms(LegacyPaymentTerms.builder().build())
            .paymentCardLastRequested(LocalDate.now().minusWeeks(1))
            .lastEnforcement("12345")
            .build();
    }

    private static void assertAddPaymentTermsLegacyRequest(AddPaymentTermsLegacyRequest legacyRequest,
        long defendantAccountId, String businessUnitId,
        String businessUnitUserId, String ifMatch) {
        assertNotNull(legacyRequest);
        assertThat(legacyRequest.getDefendantAccountId()).isEqualTo(String.valueOf(defendantAccountId));
        assertThat(legacyRequest.getBusinessUnitId()).isEqualTo(businessUnitId);
        assertThat(legacyRequest.getBusinessUnitUserId()).isEqualTo(businessUnitUserId);
        assertThat(legacyRequest.getVersion()).isEqualTo(extractBigInteger(ifMatch));
    }

    private static void assertGetDefendantAccountPaymentTermsResponse(
        GetDefendantAccountPaymentTermsResponse actualResponse, AddPaymentTermsLegacyResponse legacyResponse) {

        assertNotNull(actualResponse);
        assertThat(actualResponse.getVersion()).isEqualTo(legacyResponse.getVersion());
        assertNotNull(actualResponse.getPaymentTerms());
        assertThat(actualResponse.getPaymentCardLastRequested())
            .isEqualTo(legacyResponse.getPaymentCardLastRequested());
        assertThat(actualResponse.getLastEnforcement()).isEqualTo(legacyResponse.getLastEnforcement());
    }

    @Test
    void addPaymentTerms_whenGatewayResponseWithException_thenDoNotReturnEntity() {
        // Given
        long defendantAccountId = 1L;
        String businessUnitId = "BU";
        String businessUnitUserId = "U";
        String ifMatch = "\"1\"";

        // When
        doThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
            .when(gatewayService).postToGateway(
                any(),
                any(),
                any(),
                any()
            );

        // Then
        assertThrows(
            HttpServerErrorException.class, () ->
                legacyDefendantAccountService.addPaymentTerms(
                    defendantAccountId, businessUnitId,
                    businessUnitUserId, "Poster Name", ifMatch,
                    "auth", null
                )
        );

        var requestCaptor = ArgumentCaptor.forClass(AddPaymentTermsLegacyRequest.class);

        verify(gatewayService, times(1))
            .postToGateway(
                eq(LegacyDefendantAccountService.ADD_PAYMENT_TERMS),
                eq(AddPaymentTermsLegacyResponse.class),
                requestCaptor.capture(),
                isNull()
            );

        assertAddPaymentTermsLegacyRequest(
            requestCaptor.getValue(), defendantAccountId, businessUnitId,
            businessUnitUserId, ifMatch
        );
    }

    @Test
    void addPaymentTerms_whenGatewayResponseWithLegacyFailure_thenStillReturnMappedResponse() {
        // Given
        long defendantAccountId = 1L;
        String businessUnitId = "BU";
        String businessUnitUserId = "U";
        String ifMatch = "\"1\"";

        var legacyResponse = createAddPaymentTermsLegacyResponse(defendantAccountId, ifMatch);
        var gateWayResponse = new GatewayService.Response<>(
            HttpStatus.SERVICE_UNAVAILABLE, legacyResponse,
            "<legacy-failure/>", null
        );

        // When
        doReturn(gateWayResponse).when(gatewayService).postToGateway(any(), any(), any(), any());

        var actualResponse = legacyDefendantAccountService.addPaymentTerms(
            defendantAccountId, businessUnitId,
            businessUnitUserId, "Poster Name", ifMatch,
            "auth", null
        );

        // Then
        var requestCaptor = ArgumentCaptor.forClass(AddPaymentTermsLegacyRequest.class);

        verify(gatewayService, times(1))
            .postToGateway(
                eq(LegacyDefendantAccountService.ADD_PAYMENT_TERMS),
                eq(AddPaymentTermsLegacyResponse.class),
                requestCaptor.capture(),
                isNull()
            );

        assertAddPaymentTermsLegacyRequest(
            requestCaptor.getValue(), defendantAccountId, businessUnitId,
            businessUnitUserId, ifMatch
        );
        assertGetDefendantAccountPaymentTermsResponse(actualResponse, legacyResponse);
    }

}
