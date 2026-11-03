package uk.gov.hmcts.opal.service.legacy;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.common.logging.LogUtil;
import uk.gov.hmcts.opal.common.legacy.config.LegacyGatewayProperties;
import uk.gov.hmcts.opal.common.legacy.model.ErrorResponse;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.common.legacy.service.LegacyGatewayService;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUserV2;
import uk.gov.hmcts.opal.util.FeatureFlags;
import uk.gov.hmcts.opal.dto.legacy.LegacyCreateDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyCreateDefendantAccountResponse;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.entity.draft.TimelineData;
import uk.gov.hmcts.opal.exception.JsonSchemaValidationException;
import uk.gov.hmcts.opal.service.opal.jpa.DraftAccountTransactional;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.service.legacy.LegacyDraftAccountPublish.ERROR_MESSAGE_TEMPLATE;

@ExtendWith(MockitoExtension.class)
@ExtendWith(OutputCaptureExtension.class)
class LegacyDraftAccountPublishTest {

    @Spy
    private MockRestClient restClient = spy(MockRestClient.class);

    @Mock
    private LegacyGatewayProperties gatewayProperties;

    private GatewayService gatewayService;

    @Mock
    private FeatureToggleApi featureToggleApi;

    @Mock
    private DraftAccountTransactional draftAccountTransactional;

    @InjectMocks
    private LegacyDraftAccountPublish legacyDraftAccountPublish;

    @BeforeEach
    void openMocks() throws Exception {
        gatewayService = spy(new LegacyGatewayService(gatewayProperties, restClient));
        injectGatewayService(legacyDraftAccountPublish, gatewayService);
    }

    private void injectGatewayService(
        LegacyDraftAccountPublish legacyDraftAccountPublish, GatewayService gatewayService)
        throws NoSuchFieldException, IllegalAccessException {

        Field field = LegacyDraftAccountPublish.class.getDeclaredField("gatewayService");
        field.setAccessible(true);
        field.set(legacyDraftAccountPublish, gatewayService);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testPublishDefendantAccount_success() {

        BusinessUnitUserV2 buu = BusinessUnitUserV2.builder()
            .businessUnitId((short)7)
            .businessUnitUserId("Dave")
            .build();
        DraftAccountEntity publish = DraftAccountEntity.builder()
            .businessUnit(
                BusinessUnitEntity.builder()
                    .businessUnitId((short)6)
                    .build())
            .timelineData(emptyTimelineData())
            .build();

        LegacyCreateDefendantAccountResponse responseBody = LegacyCreateDefendantAccountResponse.builder()
            .defendantAccountId(777L)
            .defendantAccountNumber("77-007")
            .build();

        when(restClient.responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(responseBody);

        ResponseEntity<String> serverSuccessResponse =
            new ResponseEntity<>(responseBody.toXml(), HttpStatus.valueOf(200));
        when(restClient.responseSpec.toEntity(String.class)).thenReturn(serverSuccessResponse);

        when(draftAccountTransactional
                 .updateStatus(publish, DraftAccountStatus.LEGACY_PENDING, draftAccountTransactional))
            .thenReturn(publish);
        when(draftAccountTransactional
                 .updateStatus(publish, DraftAccountStatus.PUBLISHED, draftAccountTransactional))
            .thenReturn(publish);

        DraftAccountEntity published = legacyDraftAccountPublish.publishDefendantAccount(publish, buu);

        assertEquals(publish, published);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testPublishDefendantAccount_serverError() throws JacksonException {

        // Arrange
        String opId = "1234";
        String sensitiveErrorMessage = "Something went wrong on the server.";
        try (MockedStatic<LogUtil> logUtilMock = mockStatic(LogUtil.class)) {
            logUtilMock.when(LogUtil::getOrCreateOpalOperationId).thenReturn(opId);

            BusinessUnitUserV2 buu = BusinessUnitUserV2.builder()
                .businessUnitId((short)7)
                .businessUnitUserId("Dave")
                .build();
            DraftAccountEntity publish = DraftAccountEntity.builder()
                .businessUnit(
                    BusinessUnitEntity.builder()
                        .businessUnitId((short)6)
                        .build())
                .timelineData(emptyTimelineData())
                .build();

            LegacyCreateDefendantAccountResponse responseBody = LegacyCreateDefendantAccountResponse.builder()
                .errorResponse(ErrorResponse.builder()
                    .errorCode("some code")
                    .build())
                .build();

            when(restClient.responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(responseBody);
            ResponseEntity<String> serverErrorResponse =
                new ResponseEntity<>(responseBody.toXml(), HttpStatus.INTERNAL_SERVER_ERROR);
            when(restClient.responseSpec.toEntity(String.class)).thenReturn(serverErrorResponse);

            when(draftAccountTransactional
                .updateStatus(publish, DraftAccountStatus.LEGACY_PENDING, draftAccountTransactional))
                .then(returnsFirstArg());
            when(draftAccountTransactional
                .updateStatus(publish, DraftAccountStatus.PUBLISHING_FAILED, draftAccountTransactional))
                .then(returnsFirstArg());

            // Act
            DraftAccountEntity published = legacyDraftAccountPublish.publishDefendantAccount(publish, buu);

            // Assert
            ObjectMapper mapper = new ObjectMapper();
            String publishedAsJson = mapper.writeValueAsString(published);
            assertThat(publishedAsJson).doesNotContain(sensitiveErrorMessage);

            String expectedMessage = String.format(ERROR_MESSAGE_TEMPLATE, opId);
            assertEquals(published.getStatusMessage(), expectedMessage);
            JsonNode root = mapper.readTree(published.getTimelineData());
            String reasonText = root.get(0).path("reason_text").asString();
            assertEquals(reasonText, expectedMessage);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    void testPublishDefendantAccount_400Error() {

        BusinessUnitUserV2 buu = BusinessUnitUserV2.builder()
            .businessUnitId((short)7)
            .businessUnitUserId("Dave")
            .build();
        DraftAccountEntity publish = DraftAccountEntity.builder()
            .businessUnit(
                BusinessUnitEntity.builder()
                    .businessUnitId((short)6)
                    .build())
            .timelineData(emptyTimelineData())
            .build();

        LegacyCreateDefendantAccountResponse responseBody = LegacyCreateDefendantAccountResponse.builder()
            .build();

        when(restClient.responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(responseBody);

        ResponseEntity<String> serverErrorResponse =
            new ResponseEntity<>(responseBody.toXml(), HttpStatus.NOT_FOUND);
        when(restClient.responseSpec.toEntity(String.class)).thenReturn(serverErrorResponse);

        when(draftAccountTransactional
                 .updateStatus(publish, DraftAccountStatus.LEGACY_PENDING, draftAccountTransactional))
            .thenReturn(publish);

        DraftAccountEntity published = legacyDraftAccountPublish.publishDefendantAccount(publish, buu);

        assertEquals(publish, published);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testPublishDefendantAccount_unknownError() {

        BusinessUnitUserV2 buu = BusinessUnitUserV2.builder()
            .businessUnitId((short)7)
            .businessUnitUserId("Dave")
            .build();
        DraftAccountEntity publish = DraftAccountEntity.builder()
            .businessUnit(
                BusinessUnitEntity.builder()
                    .businessUnitId((short)6)
                    .build())
            .timelineData(emptyTimelineData())
            .build();

        LegacyCreateDefendantAccountResponse responseBody = LegacyCreateDefendantAccountResponse.builder()
            .errorResponse(ErrorResponse.builder()
                .errorCode("some code")
                .errorMessage("Something went wrong on the server.")
                .build())
            .build();

        when(restClient.responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(responseBody);

        ResponseEntity<String> serverErrorResponse =
            new ResponseEntity<>(responseBody.toXml(), HttpStatus.MOVED_PERMANENTLY);
        when(restClient.responseSpec.toEntity(String.class)).thenReturn(serverErrorResponse);

        when(draftAccountTransactional
                 .updateStatus(publish, DraftAccountStatus.LEGACY_PENDING, draftAccountTransactional))
            .thenReturn(publish);
        when(draftAccountTransactional
            .updateStatus(publish, DraftAccountStatus.PUBLISHING_FAILED, draftAccountTransactional))
            .thenReturn(publish);

        DraftAccountEntity published = legacyDraftAccountPublish.publishDefendantAccount(publish, buu);

        assertEquals(publish, published);
        verify(draftAccountTransactional)
            .updateStatus(publish, DraftAccountStatus.PUBLISHING_FAILED, draftAccountTransactional);
    }

    @Test
    void testPublishDefendantAccount_exceptionResponse() throws Exception {

        GatewayService mockGatewayService = mock(GatewayService.class);
        injectGatewayService(legacyDraftAccountPublish, mockGatewayService);

        BusinessUnitUserV2 buu = BusinessUnitUserV2.builder()
            .businessUnitId((short)7)
            .businessUnitUserId("Dave")
            .build();
        DraftAccountEntity publish = DraftAccountEntity.builder()
            .businessUnit(
                BusinessUnitEntity.builder()
                    .businessUnitId((short)6)
                    .build())
            .timelineData(emptyTimelineData())
            .build();

        GatewayService.Response<LegacyCreateDefendantAccountResponse> response =
            new GatewayService.Response<>(HttpStatus.BAD_GATEWAY, new RuntimeException("boom"), null);
        when(mockGatewayService.postToGatewayAsync(
            eq(LegacyDraftAccountPublish.CREATE_DEFENDANT_ACCOUNT),
            eq(LegacyCreateDefendantAccountResponse.class),
            any(LegacyCreateDefendantAccountRequest.class),
            eq(null)))
            .thenReturn(CompletableFuture.completedFuture(response));
        when(draftAccountTransactional
                 .updateStatus(publish, DraftAccountStatus.LEGACY_PENDING, draftAccountTransactional))
            .thenReturn(publish);

        DraftAccountEntity published = legacyDraftAccountPublish.publishDefendantAccount(publish, buu);

        assertEquals(publish, published);
    }

    @Test
    void testPublishDefendantAccount_interruptedException() throws Exception {

        GatewayService mockGatewayService = mock(GatewayService.class);
        injectGatewayService(legacyDraftAccountPublish, mockGatewayService);

        BusinessUnitUserV2 buu = BusinessUnitUserV2.builder()
            .businessUnitId((short)7)
            .businessUnitUserId("Dave")
            .build();
        DraftAccountEntity publish = DraftAccountEntity.builder()
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short)6).build())
            .timelineData(emptyTimelineData())
            .build();

        CompletableFuture<GatewayService.Response<LegacyCreateDefendantAccountResponse>> future =
            new CompletableFuture<>() {
                @Override
                public GatewayService.Response<LegacyCreateDefendantAccountResponse> get()
                    throws InterruptedException {
                    throw new InterruptedException("interrupted");
                }
            };
        when(mockGatewayService.postToGatewayAsync(
            eq(LegacyDraftAccountPublish.CREATE_DEFENDANT_ACCOUNT),
            eq(LegacyCreateDefendantAccountResponse.class),
            any(LegacyCreateDefendantAccountRequest.class),
            eq(null)))
            .thenReturn(future);
        when(draftAccountTransactional
                 .updateStatus(publish, DraftAccountStatus.LEGACY_PENDING, draftAccountTransactional))
            .thenReturn(publish);

        DraftAccountEntity published = legacyDraftAccountPublish.publishDefendantAccount(publish, buu);

        assertEquals(publish, published);
        assertThat(Thread.currentThread().isInterrupted()).isTrue();
        Thread.interrupted();
    }

    @Test
    void testPublishDefendantAccount_executionException() throws Exception {

        GatewayService mockGatewayService = mock(GatewayService.class);
        injectGatewayService(legacyDraftAccountPublish, mockGatewayService);

        BusinessUnitUserV2 buu = BusinessUnitUserV2.builder()
            .businessUnitId((short)7)
            .businessUnitUserId("Dave")
            .build();
        DraftAccountEntity publish = DraftAccountEntity.builder()
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short)6).build())
            .timelineData(emptyTimelineData())
            .build();

        CompletableFuture<GatewayService.Response<LegacyCreateDefendantAccountResponse>> future =
            new CompletableFuture<>() {
                @Override
                public GatewayService.Response<LegacyCreateDefendantAccountResponse> get()
                    throws ExecutionException {
                    throw new ExecutionException(new RuntimeException("boom"));
                }
            };
        when(mockGatewayService.postToGatewayAsync(
            eq(LegacyDraftAccountPublish.CREATE_DEFENDANT_ACCOUNT),
            eq(LegacyCreateDefendantAccountResponse.class),
            any(LegacyCreateDefendantAccountRequest.class),
            eq(null)))
            .thenReturn(future);
        when(draftAccountTransactional
                 .updateStatus(publish, DraftAccountStatus.LEGACY_PENDING, draftAccountTransactional))
            .thenReturn(publish);

        assertThrows(RuntimeException.class, () -> legacyDraftAccountPublish.publishDefendantAccount(publish, buu));
    }

    @Test
    void testcreateDefendantAccountRequest_emptyAccount() {

        LegacyCreateDefendantAccountRequest lcdar = legacyDraftAccountPublish.createDefendantAccountRequest(
            DraftAccountEntity.builder()
                .businessUnit(
                    BusinessUnitEntity.builder()
                        .businessUnitId((short) 6)
                        .build())
                .account("{}")
                .build(),
            BusinessUnitUserV2.builder().businessUnitUserId("testUser").build()
        );

        assertEquals("testUser", lcdar.getBusinessUnitUserId());
        assertEquals("{}", lcdar.getDefendantAccount().toString());
    }

    @Test
    void testcreateDefendantAccountRequest_nullAccount() {

        LegacyCreateDefendantAccountRequest lcdar = legacyDraftAccountPublish.createDefendantAccountRequest(
            DraftAccountEntity.builder()
                .businessUnit(
                    BusinessUnitEntity.builder()
                        .businessUnitId((short) 6)
                        .build())
                .account(null)
                .build(),
            BusinessUnitUserV2.builder().businessUnitUserId("testUser").build()
        );

        assertEquals("testUser", lcdar.getBusinessUnitUserId());
        assertNull(lcdar.getDefendantAccount());
    }

    @Test
    void testcreateDefendantAccountRequest_blankAccount() {

        LegacyCreateDefendantAccountRequest lcdar = legacyDraftAccountPublish.createDefendantAccountRequest(
            DraftAccountEntity.builder()
                .businessUnit(
                    BusinessUnitEntity.builder()
                        .businessUnitId((short) 6)
                        .build())
                .account("   ")
                .build(),
            BusinessUnitUserV2.builder().businessUnitUserId("testUser").build()
        );

        assertEquals("testUser", lcdar.getBusinessUnitUserId());
        assertNull(lcdar.getDefendantAccount());
    }

    @Test
    void testcreateDefendantAccountRequest_invalidJson_throwsException() {
        DraftAccountEntity entity = DraftAccountEntity.builder()
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 6).build())
            .account("{invalidJson:}") // malformed JSON
            .build();
        BusinessUnitUserV2 user = BusinessUnitUserV2.builder().businessUnitUserId("testUser").build();

        assertThrows(JsonSchemaValidationException.class, () -> {
            legacyDraftAccountPublish.createDefendantAccountRequest(entity, user);
        });
    }

    @Test
    void testcreateDefendantAccountRequest() {

        LegacyCreateDefendantAccountRequest lcdar = legacyDraftAccountPublish.createDefendantAccountRequest(
            DraftAccountEntity.builder()
                .businessUnit(
                    BusinessUnitEntity.builder()
                        .businessUnitId((short) 6)
                        .build())
                .account("{\"defendantAccountId\":12345,\"accountNumber\":\"77-007\"}")
                .build(),
            BusinessUnitUserV2.builder().businessUnitUserId("testUser").build()
        );

        assertEquals("testUser", lcdar.getBusinessUnitUserId());
        assertEquals(
            new ObjectMapper().createObjectNode()
                .put("defendantAccountId", 12345)
                .put("accountNumber", "77-007"),
            new ObjectMapper().valueToTree(lcdar.getDefendantAccount())
        );
    }

    @Test
    void createDefendantAccountRequest_whenFlagEnabledAndRegistrationNull_usesNV() {
        when(featureToggleApi.isFeatureEnabledWithPropertyValueDefault(
            FeatureFlags.RELEASE_1B_1_1,
            FeatureFlags.RELEASE_1B_1_1_ENABLED_PROPERTY,
            false
        )).thenReturn(true);

        String originalAccount = """
            {
              "fp_ticket_detail": {
                "fp_registration_number": null
              }
            }
            """;

        DraftAccountEntity entity = DraftAccountEntity.builder()
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 6).build())
            .account(originalAccount)
            .build();
        BusinessUnitUserV2 user = BusinessUnitUserV2.builder()
            .businessUnitUserId("testUser")
            .build();

        LegacyCreateDefendantAccountRequest request =
            legacyDraftAccountPublish.createDefendantAccountRequest(entity, user);

        JsonNode account = new ObjectMapper().valueToTree(request.getDefendantAccount());

        assertEquals(
            "NV",
            account.get("fp_ticket_detail").get("fp_registration_number").asText()
        );
        assertEquals(originalAccount, entity.getAccount());
    }

    @Test
    void createDefendantAccountRequest_whenFlagDisabled_preservesNullRegistration() {
        when(featureToggleApi.isFeatureEnabledWithPropertyValueDefault(FeatureFlags.RELEASE_1B_1_1,
            FeatureFlags.RELEASE_1B_1_1_ENABLED_PROPERTY, false)).thenReturn(false);

        DraftAccountEntity entity = DraftAccountEntity.builder()
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 6).build())
            .account("""
            {
              "fp_ticket_detail": {
                "fp_registration_number": null
              }
            }
            """)
            .build();
        BusinessUnitUserV2 user = BusinessUnitUserV2.builder()
            .businessUnitUserId("testUser")
            .build();

        LegacyCreateDefendantAccountRequest request =
            legacyDraftAccountPublish.createDefendantAccountRequest(entity, user);

        JsonNode account = new ObjectMapper().valueToTree(request.getDefendantAccount());
        JsonNode registrationNumber =
            account.get("fp_ticket_detail").get("fp_registration_number");

        assertTrue(registrationNumber.isNull());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t"})
    void createDefendantAccountRequest_whenRegistrationBlank_usesNV(String registrationNumber) {
        when(featureToggleApi.isFeatureEnabledWithPropertyValueDefault(FeatureFlags.RELEASE_1B_1_1,
            FeatureFlags.RELEASE_1B_1_1_ENABLED_PROPERTY, false)).thenReturn(true);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode fpTicketDetail = objectMapper.createObjectNode()
            .put("fp_registration_number", registrationNumber);
        String accountJson = objectMapper.createObjectNode()
            .set("fp_ticket_detail", fpTicketDetail)
            .toString();

        DraftAccountEntity entity = DraftAccountEntity.builder()
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 6).build())
            .account(accountJson)
            .build();
        BusinessUnitUserV2 user = BusinessUnitUserV2.builder()
            .businessUnitUserId("testUser")
            .build();

        LegacyCreateDefendantAccountRequest request =
            legacyDraftAccountPublish.createDefendantAccountRequest(entity, user);

        JsonNode account = objectMapper.valueToTree(request.getDefendantAccount());

        assertEquals("NV", account.get("fp_ticket_detail").get("fp_registration_number").asText());
    }

    @Test
    void createDefendantAccountRequest_whenRegistrationHasContent_preservesOriginalValue() {
        when(featureToggleApi.isFeatureEnabledWithPropertyValueDefault(FeatureFlags.RELEASE_1B_1_1,
            FeatureFlags.RELEASE_1B_1_1_ENABLED_PROPERTY, false)).thenReturn(true);

        String registrationNumber = " ABC123 ";
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode fpTicketDetail = objectMapper.createObjectNode()
            .put("fp_registration_number", registrationNumber);
        String accountJson = objectMapper.createObjectNode()
            .set("fp_ticket_detail", fpTicketDetail)
            .toString();

        DraftAccountEntity entity = DraftAccountEntity.builder()
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 6).build())
            .account(accountJson)
            .build();
        BusinessUnitUserV2 user = BusinessUnitUserV2.builder()
            .businessUnitUserId("testUser")
            .build();

        LegacyCreateDefendantAccountRequest request =
            legacyDraftAccountPublish.createDefendantAccountRequest(entity, user);

        JsonNode account = objectMapper.valueToTree(request.getDefendantAccount());

        assertEquals(
            registrationNumber,
            account.get("fp_ticket_detail").get("fp_registration_number").asText()
        );
    }

    @Test
    void createDefendantAccountRequest_whenRegistrationOmitted_usesNV() {
        when(featureToggleApi.isFeatureEnabledWithPropertyValueDefault(
            FeatureFlags.RELEASE_1B_1_1,
            FeatureFlags.RELEASE_1B_1_1_ENABLED_PROPERTY,
            false
        )).thenReturn(true);

        DraftAccountEntity entity = DraftAccountEntity.builder()
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 6).build())
            .account("""
            {
              "fp_ticket_detail": {
                "notice_number": "TEST123"
              }
            }
            """)
            .build();
        BusinessUnitUserV2 user = BusinessUnitUserV2.builder()
            .businessUnitUserId("testUser")
            .build();

        LegacyCreateDefendantAccountRequest request =
            legacyDraftAccountPublish.createDefendantAccountRequest(entity, user);

        JsonNode account = new ObjectMapper().valueToTree(request.getDefendantAccount());

        assertEquals(
            "NV",
            account.get("fp_ticket_detail").get("fp_registration_number").asText()
        );
    }

    @Test
    void createDefendantAccountRequest_whenFpTicketDetailAbsent_doesNotAddIt() {
        when(featureToggleApi.isFeatureEnabledWithPropertyValueDefault(
            FeatureFlags.RELEASE_1B_1_1,
            FeatureFlags.RELEASE_1B_1_1_ENABLED_PROPERTY,
            false
        )).thenReturn(true);

        DraftAccountEntity entity = DraftAccountEntity.builder()
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 6).build())
            .account("""
            {
              "defendant_account_id": 12345
            }
            """)
            .build();
        BusinessUnitUserV2 user = BusinessUnitUserV2.builder()
            .businessUnitUserId("testUser")
            .build();

        LegacyCreateDefendantAccountRequest request =
            legacyDraftAccountPublish.createDefendantAccountRequest(entity, user);

        JsonNode account = new ObjectMapper().valueToTree(request.getDefendantAccount());

        assertFalse(account.has("fp_ticket_detail"));
    }

    @Test
    void createDefendantAccountRequest_whenFpTicketDetailNull_preservesNull() {
        when(featureToggleApi.isFeatureEnabledWithPropertyValueDefault(
            FeatureFlags.RELEASE_1B_1_1,
            FeatureFlags.RELEASE_1B_1_1_ENABLED_PROPERTY,
            false
        )).thenReturn(true);

        DraftAccountEntity entity = DraftAccountEntity.builder()
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 6).build())
            .account("""
            {
              "fp_ticket_detail": null
            }
            """)
            .build();
        BusinessUnitUserV2 user = BusinessUnitUserV2.builder()
            .businessUnitUserId("testUser")
            .build();

        LegacyCreateDefendantAccountRequest request =
            legacyDraftAccountPublish.createDefendantAccountRequest(entity, user);

        JsonNode account = new ObjectMapper().valueToTree(request.getDefendantAccount());

        assertTrue(account.get("fp_ticket_detail").isNull());
    }

    private String emptyTimelineData() {
        return new TimelineData().toJson();
    }

}
