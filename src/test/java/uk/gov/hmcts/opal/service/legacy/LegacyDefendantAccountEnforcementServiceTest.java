package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.AddDefendantAccountEnforcementRequest;
import uk.gov.hmcts.opal.dto.AddEnforcementResponse;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.dto.PaymentTerms;
import uk.gov.hmcts.opal.dto.ResultResponse;
import uk.gov.hmcts.opal.dto.common.InstalmentPeriod;
import uk.gov.hmcts.opal.dto.common.PaymentTermsType;
import uk.gov.hmcts.opal.dto.legacy.AddDefendantAccountEnforcementLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountEnforcementStatusResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountEnforcementStatusResponse.EnforcementAction;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountEnforcementStatusResponse.EnforcementOverview;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.legacy.common.CollectionOrder;
import uk.gov.hmcts.opal.dto.legacy.common.CourtReference;
import uk.gov.hmcts.opal.dto.legacy.common.EnforcementOverride;
import uk.gov.hmcts.opal.dto.legacy.common.EnforcementOverrideResultReference;
import uk.gov.hmcts.opal.dto.legacy.common.EnforcerReference;
import uk.gov.hmcts.opal.dto.legacy.common.LjaReference;
import uk.gov.hmcts.opal.dto.legacy.common.ResultReference;
import uk.gov.hmcts.opal.dto.legacy.common.ResultResponses;
import uk.gov.hmcts.opal.entity.court.CourtEntity.Lite;
import uk.gov.hmcts.opal.generated.model.AccountStatusReferenceCommon;
import uk.gov.hmcts.opal.generated.model.AccountStatusReferenceCommon.AccountStatusCodeEnum;
import uk.gov.hmcts.opal.generated.model.EnforcementActionDefendantAccount;
import uk.gov.hmcts.opal.generated.model.EnforcementOverrideCommon;
import uk.gov.hmcts.opal.generated.model.EnforcementOverviewDefendantAccount;
import uk.gov.hmcts.opal.service.opal.CourtService;

@ExtendWith(MockitoExtension.class)
public class LegacyDefendantAccountEnforcementServiceTest {

    @Spy
    private MockRestClient restClient = spy(MockRestClient.class);

    @Mock
    private LegacyGatewayProperties gatewayProperties;

    @Mock
    private CourtService courtService;

    private GatewayService gatewayService;

    @InjectMocks
    private  LegacyDefendantAccountEnforcementService legacyDefendantAccountEnforcementService;

    @BeforeEach
    void openMocks() throws Exception {
        gatewayService = Mockito.spy(new LegacyGatewayService(gatewayProperties, restClient));
        injectGatewayService(legacyDefendantAccountEnforcementService, gatewayService);

    }

    private void injectGatewayService(
        LegacyDefendantAccountEnforcementService legacyDefendantAccountService, GatewayService gatewayService)
        throws NoSuchFieldException, IllegalAccessException {

        Field field = LegacyDefendantAccountEnforcementService.class.getDeclaredField("gatewayService");
        field.setAccessible(true);
        field.set(legacyDefendantAccountService, gatewayService);

    }

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
            legacyDefendantAccountEnforcementService.addEnforcement(123L, "BU-1", "user-1", "\"1\"", "auth", null);

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

        // Act
        AddEnforcementResponse out =
            legacyDefendantAccountEnforcementService.addEnforcement(500L, "BU-500", "user-500", "\"5\"", "auth", null);

        // Assert
        assertNotNull(out);
        assertEquals("ENF-500", out.getEnforcementId());
        assertEquals("500", out.getDefendantAccountId());
        assertEquals(5, out.getVersion());
    }


    @Test
    @SuppressWarnings("unchecked")
    void addEnforcement_withRequest_sendsLegacyRequestContainingMappedCollectionsAndPaymentTerms() throws Exception {
        // Arrange: mock modern request with one ResultResponse and a PaymentTerms object
        AddDefendantAccountEnforcementRequest request = mock(AddDefendantAccountEnforcementRequest.class);
        ResultResponse rr = mock(ResultResponse.class);
        when(rr.getParameterName()).thenReturn("param-1");
        when(rr.getResponse()).thenReturn("resp-1");
        when(request.getEnforcementResultResponses()).thenReturn(java.util.List.of(rr));

        PaymentTerms pt = mock(PaymentTerms.class);
        PaymentTermsType ptt = mock(PaymentTermsType.class);
        when(ptt.getPaymentTermsTypeCode()).thenReturn(null); // safe for mapLegacyPaymentTermsType
        InstalmentPeriod ip = mock(InstalmentPeriod.class);
        when(ip.getInstalmentPeriodCode()).thenReturn(null); // safe for mapLegacyInstalmentPeriod
        when(pt.getPaymentTermsType()).thenReturn(ptt);
        when(pt.getInstalmentPeriod()).thenReturn(ip);
        when(request.getPaymentTerms()).thenReturn(pt);

        // Prepare legacy response entity that will be returned by gateway
        AddDefendantAccountEnforcementLegacyResponse legacyResp =
            mock(AddDefendantAccountEnforcementLegacyResponse.class);
        when(legacyResp.getEnforcementId()).thenReturn("ENF-CAP");
        when(legacyResp.getDefendantAccountId()).thenReturn("999");
        when(legacyResp.getVersion()).thenReturn(11);

        GatewayService.Response<AddDefendantAccountEnforcementLegacyResponse> resp =
            new GatewayService.Response<>(HttpStatus.OK, legacyResp, null, null);

        // stub gateway to capture the legacy request and return the response
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Object> reqCaptor = ArgumentCaptor.forClass(Object.class);
        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.ADD_ENFORCEMENT),
            eq(AddDefendantAccountEnforcementLegacyResponse.class),
            reqCaptor.capture(),
            Mockito.nullable(String.class)
        );

        // Act
        AddEnforcementResponse out =
            legacyDefendantAccountEnforcementService
                .addEnforcement(999L, "BU-TEST", "user-test",
                    "\"11\"", "auth", request);

        // Assert - public DTO returned correctly
        assertNotNull(out);
        assertEquals("ENF-CAP", out.getEnforcementId());
        assertEquals("999", out.getDefendantAccountId());
        assertEquals(11, out.getVersion());

        // Also assert the service built a legacy request with expected top-level fields and mapped collections
        Object sentLegacyRequest = reqCaptor.getValue();
        assertNotNull(sentLegacyRequest);

        // Use reflection to verify the legacy request's fields (getters created by builder)
        var clazz = sentLegacyRequest.getClass();
        var defId = clazz.getMethod("getDefendantAccountId").invoke(sentLegacyRequest);
        var buId = clazz.getMethod("getBusinessUnitId").invoke(sentLegacyRequest);
        var buUser = clazz.getMethod("getBusinessUnitUserId").invoke(sentLegacyRequest);


        assertEquals("999", defId);
        assertEquals("BU-TEST", buId);
        assertEquals("user-test", buUser);
        var version = clazz.getMethod("getVersion").invoke(sentLegacyRequest);

        assertEquals(11, ((Number) version).intValue());

        var enforcementList = clazz.getMethod("getEnforcementResultResponses").invoke(sentLegacyRequest);

        assertNotNull(enforcementList);
        assertTrue(((java.util.Collection<?>) enforcementList).size() >= 1);

        var paymentTermsLegacy = clazz.getMethod("getPaymentTerms").invoke(sentLegacyRequest);

        assertNotNull(paymentTermsLegacy);
    }

    @Test
    void addEnforcement_legacyFailure5xx_withEntity_stillReturnsMappedResponse_simpleCoverage() {
        // Arrange - legacy 5xx but responseEntity present (exercises legacy-failure logging path)
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

        // Act
        AddEnforcementResponse out =
            legacyDefendantAccountEnforcementService.addEnforcement(500L, "BU-500", "user-500", "\"5\"", "auth", null);

        // Assert
        assertNotNull(out);
        assertEquals("ENF-500", out.getEnforcementId());
        assertEquals("500", out.getDefendantAccountId());
        assertEquals(5, out.getVersion());
    }

    @Test
    void addEnforcement_gatewayResponseWithException_throwsNullPointerDueToMissingEntity() {
        // Arrange: simulate gateway returning Response with exception and no responseEntity
        GatewayService.Response<AddDefendantAccountEnforcementLegacyResponse> errResp =
            new GatewayService.Response<>(HttpStatus.BAD_GATEWAY, new RuntimeException("boom"), "<err/>");

        doReturn(errResp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.ADD_ENFORCEMENT),
            eq(AddDefendantAccountEnforcementLegacyResponse.class),
            any(),
            Mockito.nullable(String.class)
        );

        // Act & Assert: calling the public method should throw a NullPointerException inside production code
        assertThrows(NullPointerException.class, () ->
            legacyDefendantAccountEnforcementService.addEnforcement(1L, "BU", "U", "\"1\"", "auth", null)
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetEnforcementStatus_success() {
        // Arrange
        LegacyGetDefendantAccountEnforcementStatusResponse responseBody =
            createLegacyEnforcementStatusResponse(true);

        when(restClient.responseSpec
            .body(Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountEnforcementStatusResponse>>any()))
            .thenReturn(responseBody);

        when(courtService.getCourtById(anyLong())).thenReturn(Lite.builder().courtCode((short)123).build());

        ResponseEntity<String> serverSuccessResponse =
            new ResponseEntity<>(responseBody.toXml(), HttpStatus.OK);
        when(restClient.responseSpec.toEntity(String.class)).thenReturn(serverSuccessResponse);

        // Act
        EnforcementStatus response = legacyDefendantAccountEnforcementService
            .getEnforcementStatus(33L);

        // Assert
        assertNotNull(response);
        assertTrue(response.getEmployerFlag());
        assertEquals(new BigInteger("1234567890123456789012345678901234567890"), response.getVersion());
        assertFalse(response.getIsHmrcCheckEligible());
        assertNull(response.getNextEnforcementActionData());
        assertNotNull(response.getEnforcementOverride());
        assertNotNull(response.getLastEnforcementAction());
        assertNotNull(response.getEnforcementOverview());
        assertNotNull(response.getAccountStatusReference());

        EnforcementOverrideCommon override = response.getEnforcementOverride();
        assertNotNull(override.getEnforcementOverrideResult());
        assertEquals("AAB", override.getEnforcementOverrideResult().getEnforcementOverrideResultId());
        assertEquals("AaAaBb", override.getEnforcementOverrideResult().getEnforcementOverrideResultName());
        assertNotNull(override.getEnforcer());
        assertEquals(2L, override.getEnforcer().getEnforcerId());
        assertEquals("Arthur", override.getEnforcer().getEnforcerName());
        assertNotNull(override.getLja());
        assertEquals(1, override.getLja().getLjaId());
        assertEquals("England", override.getLja().getLjaName());

        EnforcementActionDefendantAccount action = response.getLastEnforcementAction();
        assertEquals("late", action.getReason());
        assertEquals("123", action.getWarrantNumber());
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), action.getDateAdded());
        assertNotNull(action.getEnforcer());
        assertEquals(4L, action.getEnforcer().getEnforcerId());
        assertEquals("Merlin", action.getEnforcer().getEnforcerName());
        assertNotNull(action.getEnforcementAction());
        assertEquals("FEE", action.getEnforcementAction().getResultId());
        assertEquals("Result Ref", action.getEnforcementAction().getResultTitle());
        assertNotNull(action.getResultResponses());
        assertNotNull(action.getResultResponses().getFirst());
        assertEquals("Param Name", action.getResultResponses().getFirst().getParameterName());
        assertEquals("A response", action.getResultResponses().getFirst().getResponse());

        EnforcementOverviewDefendantAccount overview = response.getEnforcementOverview();
        assertEquals(6, overview.getDaysInDefault());
        assertNotNull(overview.getCollectionOrder());
        assertEquals(true, overview.getCollectionOrder().getCollectionOrderFlag());
        assertEquals(LocalDate.of(2024, 3, 4), overview.getCollectionOrder().getCollectionOrderDate());
        assertNotNull(overview.getEnforcementCourt());
        assertEquals(3, overview.getEnforcementCourt().getCourtId());
        assertEquals(123, overview.getEnforcementCourt().getCourtCode());
        assertEquals("Bath", overview.getEnforcementCourt().getCourtName());

        AccountStatusReferenceCommon statusRef = response.getAccountStatusReference();
        assertEquals(AccountStatusCodeEnum.L, statusRef.getAccountStatusCode());
        assertEquals("Alive", statusRef.getAccountStatusDisplayName());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetEnforcementStatus_successMinimal() {
        // Arrange
        LegacyGetDefendantAccountEnforcementStatusResponse responseBody =
            createLegacyEnforcementStatusResponse(false);

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountEnforcementStatusResponse>>any()
        )).thenReturn(responseBody);

        ResponseEntity<String> serverSuccessResponse = new ResponseEntity<>(responseBody.toXml(), HttpStatus.OK);
        when(restClient.responseSpec.toEntity(String.class)).thenReturn(serverSuccessResponse);
        when(courtService.getCourtById(anyLong())).thenReturn(Lite.builder().courtCode((short)123).build());

        // Act
        EnforcementStatus response = legacyDefendantAccountEnforcementService.getEnforcementStatus(72L);

        // Assert
        assertNotNull(response);
        assertTrue(response.getEmployerFlag());
        assertEquals(new BigInteger("1234567890123456789012345678901234567890"), response.getVersion());
        assertFalse(response.getIsHmrcCheckEligible());
        assertNull(response.getNextEnforcementActionData());
        assertNull(response.getEnforcementOverride());
        assertNull(response.getLastEnforcementAction());
        assertNotNull(response.getEnforcementOverview());
        assertNotNull(response.getAccountStatusReference());

        EnforcementOverviewDefendantAccount overview = response.getEnforcementOverview();
        assertEquals(6, overview.getDaysInDefault());
        assertNotNull(overview.getCollectionOrder());
        assertEquals(true, overview.getCollectionOrder().getCollectionOrderFlag());
        assertEquals(LocalDate.of(2024, 3, 4), overview.getCollectionOrder().getCollectionOrderDate());
        assertNotNull(overview.getEnforcementCourt());
        assertEquals(3, overview.getEnforcementCourt().getCourtId());
        assertEquals("Bath", overview.getEnforcementCourt().getCourtName());

        AccountStatusReferenceCommon statusRef = response.getAccountStatusReference();
        assertEquals(AccountStatusCodeEnum.L, statusRef.getAccountStatusCode());
        assertEquals("Alive", statusRef.getAccountStatusDisplayName());
    }

    @Test
    void testGetEnforcementStatus_throwsRuntimeException() {
        // Arrange
        doThrow(new RuntimeException("boom"))
            .when(gatewayService)
            .postToGateway(any(), any(), any(), any());

        // Act + Assert
        assertThrows(RuntimeException.class, () -> legacyDefendantAccountEnforcementService.getEnforcementStatus(1L));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetEnforcementStatus_returnsNull() {
        // Arrange
        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountPaymentTermsResponse>>any())).thenReturn(null);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>("<error/>", HttpStatus.INTERNAL_SERVER_ERROR));

        // Act
        EnforcementStatus response = legacyDefendantAccountEnforcementService.getEnforcementStatus(42L);

        // Assert
        assertNull(response);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetEnforcementStatus_returnsFailure() {

        // Arrange
        LegacyGetDefendantAccountEnforcementStatusResponse responseBody =
            createLegacyEnforcementStatusResponse(false);
        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountEnforcementStatusResponse>>any()
        )).thenReturn(responseBody);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(responseBody.toXml(), HttpStatus.SERVICE_UNAVAILABLE));
        when(courtService.getCourtById(anyLong())).thenReturn(Lite.builder().courtCode((short)123).build());

        EnforcementStatus response = legacyDefendantAccountEnforcementService.getEnforcementStatus(66L);

        assertNotNull(response);
        assertTrue(response.getEmployerFlag());
        assertEquals(new BigInteger("1234567890123456789012345678901234567890"), response.getVersion());
        assertFalse(response.getIsHmrcCheckEligible());
        assertNull(response.getNextEnforcementActionData());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetEnforcementStatus_courtNotFoundInOpalDB() {

        // Arrange
        LegacyGetDefendantAccountEnforcementStatusResponse responseBody =
            createLegacyEnforcementStatusResponse(false);
        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountEnforcementStatusResponse>>any()
        )).thenReturn(responseBody);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(responseBody.toXml(), HttpStatus.SERVICE_UNAVAILABLE));
        when(courtService.getCourtById(anyLong())).thenThrow(new EntityNotFoundException("Court not found"));

        EntityNotFoundException error = assertThrows(EntityNotFoundException.class,
            () -> legacyDefendantAccountEnforcementService.getEnforcementStatus(66L));

        assertNotNull(error);
        assertEquals("Court not found", error.getMessage());
    }

    private LegacyGetDefendantAccountEnforcementStatusResponse createLegacyEnforcementStatusResponse(boolean full) {
        return LegacyGetDefendantAccountEnforcementStatusResponse.builder()
            .accountStatusReference(
                uk.gov.hmcts.opal.dto.legacy.common.AccountStatusReference.builder()
                    .accountStatusCode("L")
                    .accountStatusDisplayName("Alive")
                    .build())
            .enforcementOverride(full ? EnforcementOverride.builder()  // Optional
                .lja(LjaReference.builder()
                    .ljaId(1).ljaName("England").build())
                .enforcer(EnforcerReference.builder()
                    .enforcerId(2L).enforcerName("Arthur").build())
                .enforcementOverrideResult(EnforcementOverrideResultReference.builder()
                    .enforcementOverrideResultId("AAB").enforcementOverrideResultName("AaAaBb").build())
                .build() : null)
            .enforcementOverview(EnforcementOverview.builder()
                .enforcementCourt(CourtReference.builder()
                    .courtId(3L).courtName("Bath").build())
                .collectionOrder(CollectionOrder.builder()
                    .collectionOrderCode("XX").collectionOrderFlag(true)
                    .collectionOrderDate(LocalDate.of(2024, 3,4)).build())
                .daysInDefault(6)
                .build())
            .lastEnforcementAction(full ? EnforcementAction.builder() // Optional
                .enforcer(EnforcerReference.builder()
                    .enforcerId(4L).enforcerName("Merlin").build())
                .resultReference(ResultReference.builder()
                    .resultId("FEE").resultTitle("Result Ref").build())
                .resultResponses(ResultResponses.builder()
                    .parameterName("Param Name").response("A response").build())
                .dateAdded("2024-01-01T10:00:00")
                .reason("late")
                .warrantNumber("123")
                .build() : null)
            .version("1234567890123456789012345678901234567890")
            .employerFlag("true")
            .build();
    }
}
