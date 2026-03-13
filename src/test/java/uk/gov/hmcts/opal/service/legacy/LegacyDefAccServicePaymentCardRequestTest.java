package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.opal.dto.AddPaymentCardRequestResponse;
import uk.gov.hmcts.opal.dto.legacy.AddPaymentCardLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.AddPaymentCardLegacyResponse;

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
            legacyDefendantAccountService.addPaymentCardRequest(123L, "78", null, "4", "AUTH");

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

        legacyDefendantAccountService.addPaymentCardRequest(123L, "78", null, "9", "AUTH");

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
            legacyDefendantAccountService.addPaymentCardRequest(99L, "78", null, "1", "AUTH")
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
            legacyDefendantAccountService.addPaymentCardRequest(88L, "78", null, "2", "AUTH")
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
            legacyDefendantAccountService.addPaymentCardRequest(55L, "78", null, "3", "AUTH")
        );
    }

    @Test
    void addPaymentCardRequest_legacy_invalidIfMatchThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            legacyDefendantAccountService.addPaymentCardRequest(1L, "78", null, "notANumber", "AUTH")
        );
    }
}
