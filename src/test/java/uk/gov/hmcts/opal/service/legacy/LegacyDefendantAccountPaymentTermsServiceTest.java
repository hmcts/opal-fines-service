package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.AddPaymentCardRequestResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.legacy.AddPaymentCardLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.AddPaymentCardLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.service.opal.CourtService;

@ExtendWith(MockitoExtension.class)
public class LegacyDefendantAccountPaymentTermsServiceTest {

    @Spy
    private MockRestClient restClient = spy(MockRestClient.class);

    @Mock
    private LegacyGatewayProperties gatewayProperties;

    @Mock
    private CourtService courtService;

    private GatewayService gatewayService;

    @InjectMocks
    private  LegacyDefendantAccountPaymentTermsService legacyDefendantAccountPaymentTermsService;

    @BeforeEach
    void openMocks() throws Exception {
        gatewayService = Mockito.spy(new LegacyGatewayService(gatewayProperties, restClient));
        injectGatewayService(legacyDefendantAccountPaymentTermsService, gatewayService);

    }

    private void injectGatewayService(
        LegacyDefendantAccountPaymentTermsService legacyDefendantAccountService, GatewayService gatewayService)
        throws NoSuchFieldException, IllegalAccessException {

        Field field = LegacyDefendantAccountPaymentTermsService.class.getDeclaredField("gatewayService");
        field.setAccessible(true);
        field.set(legacyDefendantAccountService, gatewayService);

    }


    @Test
    void addPaymentCardRequest_legacy_happyPath() {

        // Given
        AddPaymentCardLegacyResponse legacyResp =
            new AddPaymentCardLegacyResponse("123", "4");

        GatewayService.Response<AddPaymentCardLegacyResponse> gwResp =
            new GatewayService.Response<>(HttpStatus.OK, legacyResp, null, null);

        doReturn(gwResp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.ADD_PAYMENT_CARD_REQUEST),
            eq(AddPaymentCardLegacyResponse.class),
            any(AddPaymentCardLegacyRequest.class),
            isNull()
        );

        // When
        AddPaymentCardRequestResponse out =
            legacyDefendantAccountPaymentTermsService.addPaymentCardRequest(123L, "78", null,"4", "AUTH");

        // Then
        assertNotNull(out);
        assertEquals(123L, out.getDefendantAccountId());
    }

    @Test
    void addPaymentCardRequest_legacy_buildsCorrectRequest() {

        // Given
        GatewayService.Response<AddPaymentCardLegacyResponse> gwResp =
            new GatewayService.Response<>(HttpStatus.OK,
                new AddPaymentCardLegacyResponse("123", "4"),
                null, null);

        ArgumentCaptor<AddPaymentCardLegacyRequest> captor =
            ArgumentCaptor.forClass(AddPaymentCardLegacyRequest.class);

        doReturn(gwResp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.ADD_PAYMENT_CARD_REQUEST),
            eq(AddPaymentCardLegacyResponse.class),
            captor.capture(),
            isNull()
        );

        // When
        legacyDefendantAccountPaymentTermsService.addPaymentCardRequest(123L, "78",null, "9", "AUTH");

        // Then
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
            legacyDefendantAccountPaymentTermsService.addPaymentCardRequest(99L, "78", null,"1", "AUTH")
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
            legacyDefendantAccountPaymentTermsService.addPaymentCardRequest(88L, "78", null,"2", "AUTH")
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
            legacyDefendantAccountPaymentTermsService.addPaymentCardRequest(55L, "78", null, "3", "AUTH")
        );
    }

    @Test
    void addPaymentCardRequest_legacy_invalidIfMatchThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            legacyDefendantAccountPaymentTermsService.addPaymentCardRequest(1L, "78", null,"notANumber", "AUTH")
        );
    }

    @Test
    void legacyPaymentTerms_nonNullEnums_areConverted() {

        var legacy = LegacyGetDefendantAccountPaymentTermsResponse.builder()
            .version(1L)
            .paymentTerms(
                uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTerms.builder()
                    .paymentTermsType(new uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType(
                        uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType.PaymentTermsTypeCode.B))
                    .instalmentPeriod(new uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod(
                        uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod.InstalmentPeriodCode.W))
                    .build()
            )
            .build();

        doReturn(new GatewayService.Response<>(HttpStatus.OK, legacy, null, null))
            .when(gatewayService).postToGateway(eq(LegacyDefendantAccountService.GET_PAYMENT_TERMS),
                eq(LegacyGetDefendantAccountPaymentTermsResponse.class), any(), any());

        GetDefendantAccountPaymentTermsResponse out =
            legacyDefendantAccountPaymentTermsService.getPaymentTerms(123L);

        assertNotNull(out.getPaymentTerms().getPaymentTermsType());
        assertNotNull(out.getPaymentTerms().getInstalmentPeriod());
    }
}
