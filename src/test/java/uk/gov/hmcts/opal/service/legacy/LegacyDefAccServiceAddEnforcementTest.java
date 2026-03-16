package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.opal.dto.AddDefendantAccountEnforcementRequest;
import uk.gov.hmcts.opal.dto.AddEnforcementResponse;
import uk.gov.hmcts.opal.dto.PaymentTerms;
import uk.gov.hmcts.opal.dto.ResultResponse;
import uk.gov.hmcts.opal.dto.common.InstalmentPeriod;
import uk.gov.hmcts.opal.dto.common.PaymentTermsType;
import uk.gov.hmcts.opal.dto.legacy.AddDefendantAccountEnforcementLegacyResponse;

class LegacyDefAccServiceAddEnforcementTest extends AbstractLegacyDefAccServiceTest {

    @Test
    void addEnforcement_success_returnsMappedAddEnforcementResponse_simple() {
        AddDefendantAccountEnforcementLegacyResponse legacyResp =
            mock(AddDefendantAccountEnforcementLegacyResponse.class);
        when(legacyResp.getEnforcementId()).thenReturn("ENF-1");
        when(legacyResp.getDefendantAccountId()).thenReturn("123");
        when(legacyResp.getVersion()).thenReturn(1);

        GatewayService.Response<AddDefendantAccountEnforcementLegacyResponse> resp =
            new GatewayService.Response<>(HttpStatus.OK, legacyResp, null, null);

        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.ADD_ENFORCEMENT),
            eq(AddDefendantAccountEnforcementLegacyResponse.class),
            any(),
            Mockito.nullable(String.class)
        );

        AddEnforcementResponse out =
            legacyDefendantAccountService.addEnforcement(123L, "BU-1", "user-1", "\"1\"", "auth", null);

        assertNotNull(out);
        assertEquals("ENF-1", out.getEnforcementId());
        assertEquals("123", out.getDefendantAccountId());
        assertEquals(1, out.getVersion());
    }

    @Test
    void addEnforcement_legacyFailure5xx_withEntity_stillReturnsMappedResponse_simple() {
        AddDefendantAccountEnforcementLegacyResponse legacyResp =
            mock(AddDefendantAccountEnforcementLegacyResponse.class);
        when(legacyResp.getEnforcementId()).thenReturn("ENF-500");
        when(legacyResp.getDefendantAccountId()).thenReturn("500");
        when(legacyResp.getVersion()).thenReturn(5);

        GatewayService.Response<AddDefendantAccountEnforcementLegacyResponse> resp =
            new GatewayService.Response<>(HttpStatus.SERVICE_UNAVAILABLE, legacyResp, "<legacy-failure/>", null);

        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.ADD_ENFORCEMENT),
            eq(AddDefendantAccountEnforcementLegacyResponse.class),
            any(),
            Mockito.nullable(String.class)
        );

        AddEnforcementResponse out =
            legacyDefendantAccountService.addEnforcement(500L, "BU-500", "user-500", "\"5\"", "auth", null);

        assertNotNull(out);
        assertEquals("ENF-500", out.getEnforcementId());
        assertEquals("500", out.getDefendantAccountId());
        assertEquals(5, out.getVersion());
    }

    @Test
    @SuppressWarnings("unchecked")
    void addEnforcement_withRequest_sendsLegacyRequestContainingMappedCollectionsAndPaymentTerms() throws Exception {
        AddDefendantAccountEnforcementRequest request = mock(AddDefendantAccountEnforcementRequest.class);
        ResultResponse rr = mock(ResultResponse.class);
        when(rr.getParameterName()).thenReturn("param-1");
        when(rr.getResponse()).thenReturn("resp-1");
        when(request.getEnforcementResultResponses()).thenReturn(List.of(rr));

        PaymentTerms pt = mock(PaymentTerms.class);
        PaymentTermsType ptt = mock(PaymentTermsType.class);
        when(ptt.getPaymentTermsTypeCode()).thenReturn(null);
        InstalmentPeriod ip = mock(InstalmentPeriod.class);
        when(ip.getInstalmentPeriodCode()).thenReturn(null);
        when(pt.getPaymentTermsType()).thenReturn(ptt);
        when(pt.getInstalmentPeriod()).thenReturn(ip);
        when(request.getPaymentTerms()).thenReturn(pt);

        AddDefendantAccountEnforcementLegacyResponse legacyResp =
            mock(AddDefendantAccountEnforcementLegacyResponse.class);
        when(legacyResp.getEnforcementId()).thenReturn("ENF-CAP");
        when(legacyResp.getDefendantAccountId()).thenReturn("999");
        when(legacyResp.getVersion()).thenReturn(11);

        GatewayService.Response<AddDefendantAccountEnforcementLegacyResponse> resp =
            new GatewayService.Response<>(HttpStatus.OK, legacyResp, null, null);

        ArgumentCaptor<Object> reqCaptor = ArgumentCaptor.forClass(Object.class);
        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.ADD_ENFORCEMENT),
            eq(AddDefendantAccountEnforcementLegacyResponse.class),
            reqCaptor.capture(),
            Mockito.nullable(String.class)
        );

        AddEnforcementResponse out =
            legacyDefendantAccountService.addEnforcement(999L, "BU-TEST", "user-test", "\"11\"", "auth", request);

        assertNotNull(out);
        assertEquals("ENF-CAP", out.getEnforcementId());
        assertEquals("999", out.getDefendantAccountId());
        assertEquals(11, out.getVersion());

        Object sentLegacyRequest = reqCaptor.getValue();
        assertNotNull(sentLegacyRequest);

        Class<?> clazz = sentLegacyRequest.getClass();
        Object defId = clazz.getMethod("getDefendantAccountId").invoke(sentLegacyRequest);
        Object buId = clazz.getMethod("getBusinessUnitId").invoke(sentLegacyRequest);
        Object buUser = clazz.getMethod("getBusinessUnitUserId").invoke(sentLegacyRequest);

        assertEquals("999", defId);
        assertEquals("BU-TEST", buId);
        assertEquals("user-test", buUser);

        Object version = clazz.getMethod("getVersion").invoke(sentLegacyRequest);
        assertEquals(11, ((Number) version).intValue());

        Object enforcementList = clazz.getMethod("getEnforcementResultResponses").invoke(sentLegacyRequest);
        assertNotNull(enforcementList);
        assertTrue(((Collection<?>) enforcementList).size() >= 1);

        Object paymentTermsLegacy = clazz.getMethod("getPaymentTerms").invoke(sentLegacyRequest);
        assertNotNull(paymentTermsLegacy);
    }

    @Test
    void addEnforcement_legacyFailure5xx_withEntity_stillReturnsMappedResponse_simpleCoverage() {
        AddDefendantAccountEnforcementLegacyResponse legacyResp =
            mock(AddDefendantAccountEnforcementLegacyResponse.class);
        when(legacyResp.getEnforcementId()).thenReturn("ENF-500");
        when(legacyResp.getDefendantAccountId()).thenReturn("500");
        when(legacyResp.getVersion()).thenReturn(5);

        GatewayService.Response<AddDefendantAccountEnforcementLegacyResponse> resp =
            new GatewayService.Response<>(HttpStatus.SERVICE_UNAVAILABLE, legacyResp, "<legacy-failure/>", null);

        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.ADD_ENFORCEMENT),
            eq(AddDefendantAccountEnforcementLegacyResponse.class),
            any(),
            Mockito.nullable(String.class)
        );

        AddEnforcementResponse out =
            legacyDefendantAccountService.addEnforcement(500L, "BU-500", "user-500", "\"5\"", "auth", null);

        assertNotNull(out);
        assertEquals("ENF-500", out.getEnforcementId());
        assertEquals("500", out.getDefendantAccountId());
        assertEquals(5, out.getVersion());
    }

    @Test
    void addEnforcement_gatewayResponseWithException_throwsNullPointerDueToMissingEntity() {
        GatewayService.Response<AddDefendantAccountEnforcementLegacyResponse> errResp =
            new GatewayService.Response<>(HttpStatus.BAD_GATEWAY, new RuntimeException("boom"), "<err/>");

        doReturn(errResp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.ADD_ENFORCEMENT),
            eq(AddDefendantAccountEnforcementLegacyResponse.class),
            any(),
            Mockito.nullable(String.class)
        );

        assertThrows(NullPointerException.class, () ->
            legacyDefendantAccountService.addEnforcement(1L, "BU", "U", "\"1\"", "auth", null)
        );
    }
}
