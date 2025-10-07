package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.ErrorResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyCreateDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyCreateDefendantAccountResponse;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitFullEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.entity.draft.TimelineData;
import uk.gov.hmcts.opal.exception.JsonSchemaValidationException;
import uk.gov.hmcts.opal.service.opal.jpa.DraftAccountTransactional;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LegacyDraftAccountPublishTest {

    @Spy
    private MockRestClient restClient = spy(MockRestClient.class);

    @Mock
    private LegacyGatewayProperties gatewayProperties;

    private GatewayService gatewayService;

    @Mock
    private DraftAccountTransactional draftAccountTransactional;

    @InjectMocks
    private LegacyDraftAccountPublish legacyDraftAccountPublish;

    @BeforeEach
    void openMocks() throws Exception {
        gatewayService = Mockito.spy(new LegacyGatewayService(gatewayProperties, restClient));
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

        BusinessUnitUser buu = BusinessUnitUser.builder()
            .businessUnitId((short)7)
            .businessUnitUserId("Dave")
            .build();
        DraftAccountEntity publish = DraftAccountEntity.builder()
            .businessUnit(
                BusinessUnitFullEntity.builder()
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
    void testPublishDefendantAccount_serverError() {

        BusinessUnitUser buu = BusinessUnitUser.builder()
            .businessUnitId((short)7)
            .businessUnitUserId("Dave")
            .build();
        DraftAccountEntity publish = DraftAccountEntity.builder()
            .businessUnit(
                BusinessUnitFullEntity.builder()
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
            new ResponseEntity<>(responseBody.toXml(), HttpStatus.INTERNAL_SERVER_ERROR);
        when(restClient.responseSpec.toEntity(String.class)).thenReturn(serverErrorResponse);

        when(draftAccountTransactional
                 .updateStatus(publish, DraftAccountStatus.LEGACY_PENDING, draftAccountTransactional))
            .thenReturn(publish);
        when(draftAccountTransactional
                 .updateStatus(publish, DraftAccountStatus.PUBLISHING_FAILED, draftAccountTransactional))
            .thenReturn(publish);

        DraftAccountEntity published = legacyDraftAccountPublish.publishDefendantAccount(publish, buu);

        assertEquals(publish, published);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testPublishDefendantAccount_400Error() {

        BusinessUnitUser buu = BusinessUnitUser.builder()
            .businessUnitId((short)7)
            .businessUnitUserId("Dave")
            .build();
        DraftAccountEntity publish = DraftAccountEntity.builder()
            .businessUnit(
                BusinessUnitFullEntity.builder()
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

        BusinessUnitUser buu = BusinessUnitUser.builder()
            .businessUnitId((short)7)
            .businessUnitUserId("Dave")
            .build();
        DraftAccountEntity publish = DraftAccountEntity.builder()
            .businessUnit(
                BusinessUnitFullEntity.builder()
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
    void testcreateDefendantAccountRequest_emptyAccount() {

        LegacyCreateDefendantAccountRequest lcdar = LegacyDraftAccountPublish.createDefendantAccountRequest(
            DraftAccountEntity.builder()
                .businessUnit(
                    BusinessUnitFullEntity.builder()
                        .businessUnitId((short) 6)
                        .build())
                .account("{}")
                .build(),
            BusinessUnitUser.builder().businessUnitUserId("testUser").build()
        );

        assertEquals("testUser", lcdar.getBusinessUnitUserId());
        assertEquals("{}", lcdar.getDefendantAccount().toString());
    }

    @Test
    void testcreateDefendantAccountRequest_nullAccount() {

        LegacyCreateDefendantAccountRequest lcdar = LegacyDraftAccountPublish.createDefendantAccountRequest(
            DraftAccountEntity.builder()
                .businessUnit(
                    BusinessUnitFullEntity.builder()
                        .businessUnitId((short) 6)
                        .build())
                .account(null)
                .build(),
            BusinessUnitUser.builder().businessUnitUserId("testUser").build()
        );

        assertEquals("testUser", lcdar.getBusinessUnitUserId());
        assertEquals(null, lcdar.getDefendantAccount());
    }

    @Test
    void testcreateDefendantAccountRequest_invalidJson_throwsException() {
        DraftAccountEntity entity = DraftAccountEntity.builder()
            .businessUnit(BusinessUnitFullEntity.builder().businessUnitId((short) 6).build())
            .account("{invalidJson:}") // malformed JSON
            .build();
        BusinessUnitUser user = BusinessUnitUser.builder().businessUnitUserId("testUser").build();

        assertThrows(JsonSchemaValidationException.class, () -> {
            LegacyDraftAccountPublish.createDefendantAccountRequest(entity, user);
        });
    }

    @Test
    void testcreateDefendantAccountRequest() {

        LegacyCreateDefendantAccountRequest lcdar = LegacyDraftAccountPublish.createDefendantAccountRequest(
            DraftAccountEntity.builder()
                .businessUnit(
                    BusinessUnitFullEntity.builder()
                        .businessUnitId((short) 6)
                        .build())
                .account("{\"defendantAccountId\":12345,\"accountNumber\":\"77-007\"}")
                .build(),
            BusinessUnitUser.builder().businessUnitUserId("testUser").build()
        );

        assertEquals("testUser", lcdar.getBusinessUnitUserId());
        assertEquals("{\"defendantAccountId\":12345,\"accountNumber\":\"77-007\"}",
                     lcdar.getDefendantAccount().toString());
    }

    private String emptyTimelineData() {
        return new TimelineData().toJson();
    }

}
