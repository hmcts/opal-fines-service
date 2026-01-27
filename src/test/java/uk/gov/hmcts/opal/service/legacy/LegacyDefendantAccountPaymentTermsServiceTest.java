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
import java.lang.reflect.Method;
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
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.AddPaymentCardRequestResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.PaymentTerms;
import uk.gov.hmcts.opal.dto.PostedDetails;
import uk.gov.hmcts.opal.dto.legacy.AddPaymentCardLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.AddPaymentCardLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTerms;
import uk.gov.hmcts.opal.dto.legacy.LegacyPostedDetails;
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

    @Test
    void getPaymentTerms_legacyNullResponse_returnsNull() {
        // Simulate gateway returning a Response with null responseEntity
        doReturn(new GatewayService.Response<>(HttpStatus.OK, null, null, null))
            .when(gatewayService).postToGateway(
                eq(LegacyDefendantAccountService.GET_PAYMENT_TERMS),
                eq(LegacyGetDefendantAccountPaymentTermsResponse.class),
                any(), any());

        GetDefendantAccountPaymentTermsResponse resp =
            legacyDefendantAccountPaymentTermsService.getPaymentTerms(100L);

        // When legacy responseEntity is null, service should return null
        assertNull(resp);
    }

    @Test
    void getPaymentTerms_versionNull_defaultsToOne_and_mapsPostedDetails() {
        // Build a legacy response where version is null and postedDetails present
        LegacyPostedDetails legacyPosted = new LegacyPostedDetails();
        legacyPosted.setPostedDate(LocalDateTime.of(2025, 2, 14, 9, 10, 11));
        legacyPosted.setPostedBy("u-x");
        legacyPosted.setPostedByName("User X");

        LegacyPaymentTerms legacyTerms = LegacyPaymentTerms.builder()
            .postedDetails(legacyPosted)
            .build();

        LegacyGetDefendantAccountPaymentTermsResponse legacy =
            LegacyGetDefendantAccountPaymentTermsResponse.builder()
                .version(null) // should default to BigInteger.ONE in response
                .paymentTerms(legacyTerms)
                .paymentCardLastRequested(null)
                .lastEnforcement(null)
                .build();

        doReturn(new GatewayService.Response<>(HttpStatus.OK, legacy, null, null))
            .when(gatewayService).postToGateway(
                eq(LegacyDefendantAccountService.GET_PAYMENT_TERMS),
                eq(LegacyGetDefendantAccountPaymentTermsResponse.class),
                any(), any());

        GetDefendantAccountPaymentTermsResponse out =
            legacyDefendantAccountPaymentTermsService.getPaymentTerms(200L);

        assertNotNull(out);
        // version null -> defaults to BigInteger.ONE
        assertEquals(BigInteger.ONE, out.getVersion());

        // payment terms mapped and posted details mapped correctly
        PaymentTerms pt = out.getPaymentTerms();
        assertNotNull(pt);
        PostedDetails pd = pt.getPostedDetails();
        assertNotNull(pd);
        assertEquals(
            LocalDateTime.of(2025, 2, 14, 9, 10, 11), pd.getPostedDate());
        assertEquals("u-x", pd.getPostedBy());
        assertEquals("User X", pd.getPostedByName());
    }

    @Test
    void getPaymentTerms_paymentTermsNull_returnsResponseWithNullPaymentTerms() {
        // legacy with version present but no paymentTerms
        LegacyGetDefendantAccountPaymentTermsResponse legacy =
            LegacyGetDefendantAccountPaymentTermsResponse.builder()
                .version(2L)
                .paymentTerms(null)
                .paymentCardLastRequested(LocalDate.parse("2024-01-01"))
                .lastEnforcement("LE-1")
                .build();

        doReturn(new GatewayService.Response<>(HttpStatus.OK, legacy, null, null))
            .when(gatewayService).postToGateway(
                eq(LegacyDefendantAccountService.GET_PAYMENT_TERMS),
                eq(LegacyGetDefendantAccountPaymentTermsResponse.class),
                any(), any());

        GetDefendantAccountPaymentTermsResponse out =
            legacyDefendantAccountPaymentTermsService.getPaymentTerms(300L);

        assertNotNull(out);
        assertEquals(BigInteger.valueOf(2L), out.getVersion());
        assertNull(out.getPaymentTerms());
        assertEquals(LocalDate.parse("2024-01-01"), out.getPaymentCardLastRequested());
        assertEquals("LE-1", out.getLastEnforcement());
    }

    @Test
    void toPostedDetails_reflection_nullAndNonNull() throws Exception {
        // Use reflection to call the private static toPostedDetails(LegacyPostedDetails)
        Method m = LegacyDefendantAccountPaymentTermsService.class
            .getDeclaredMethod("toPostedDetails", LegacyPostedDetails.class);
        m.setAccessible(true);

        // null -> null
        Object nullResult = m.invoke(null, (Object) null);
        assertNull(nullResult);

        // non-null -> PostedDetails with fields copied
        LegacyPostedDetails legacyPosted = new LegacyPostedDetails();
        legacyPosted.setPostedDate(
            LocalDateTime.of(2023, 12, 25, 9, 10, 11));
        legacyPosted.setPostedBy("user-99");
        legacyPosted.setPostedByName("Xmas User");

        Object mapped = m.invoke(null, legacyPosted);
        assertNotNull(mapped);

        // mapped is uk.gov.hmcts.opal.dto.PostedDetails
        PostedDetails pd = (PostedDetails) mapped;
        assertEquals(LocalDateTime.of(2023, 12, 25, 9, 10, 11),
            pd.getPostedDate());
        assertEquals("user-99", pd.getPostedBy());
        assertEquals("Xmas User", pd.getPostedByName());
    }
}
