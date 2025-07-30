package uk.gov.hmcts.opal.disco.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.LegacyCreateDefendantAccountResponse;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.draft.TimelineData;
import uk.gov.hmcts.opal.service.opal.jpa.DraftAccountTransactions;
import uk.gov.hmcts.opal.service.legacy.LegacyDraftAccountPublish;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LegacyDraftAccountPublishTest {

    @Spy
    private MockRestClient restClient = spy(MockRestClient.class);

    @Mock
    private LegacyGatewayProperties gatewayProperties;

    private GatewayService gatewayService;

    @Mock
    private DraftAccountTransactions draftAccountTransactions;

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

        BusinessUnitUser buu = BusinessUnitUser.builder()
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

        ParameterizedTypeReference typeRef = new ParameterizedTypeReference<LegacyCreateDefendantAccountResponse>(){};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(responseBody);

        ResponseEntity<String> serverSuccessResponse =
            new ResponseEntity<>(responseBody.toXml(), HttpStatus.valueOf(200));
        when(restClient.responseSpec.toEntity(String.class)).thenReturn(serverSuccessResponse);

        when(draftAccountTransactions
                 .updateStatus(publish, DraftAccountStatus.LEGACY_PENDING, draftAccountTransactions))
            .thenReturn(publish);
        when(draftAccountTransactions
                 .updateStatus(publish, DraftAccountStatus.PUBLISHED, draftAccountTransactions))
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
                BusinessUnitEntity.builder()
                    .businessUnitId((short)6)
                    .build())
            .timelineData(emptyTimelineData())
            .build();

        LegacyCreateDefendantAccountResponse responseBody = LegacyCreateDefendantAccountResponse.builder()
            .errorResponse("Something went wrong on the server.")
            .build();

        ParameterizedTypeReference typeRef = new ParameterizedTypeReference<LegacyCreateDefendantAccountResponse>(){};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(responseBody);

        ResponseEntity<String> serverErrorResponse =
            new ResponseEntity<>(responseBody.toXml(), HttpStatus.INTERNAL_SERVER_ERROR);
        when(restClient.responseSpec.toEntity(String.class)).thenReturn(serverErrorResponse);

        when(draftAccountTransactions
                 .updateStatus(publish, DraftAccountStatus.LEGACY_PENDING, draftAccountTransactions))
            .thenReturn(publish);
        when(draftAccountTransactions
                 .updateStatus(publish, DraftAccountStatus.PUBLISHING_FAILED, draftAccountTransactions))
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
                BusinessUnitEntity.builder()
                    .businessUnitId((short)6)
                    .build())
            .timelineData(emptyTimelineData())
            .build();

        LegacyCreateDefendantAccountResponse responseBody = LegacyCreateDefendantAccountResponse.builder()
            .errorResponse("Something went wrong on the server.")
            .build();

        ParameterizedTypeReference typeRef = new ParameterizedTypeReference<LegacyCreateDefendantAccountResponse>(){};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(responseBody);

        ResponseEntity<String> serverErrorResponse =
            new ResponseEntity<>(responseBody.toXml(), HttpStatus.NOT_FOUND);
        when(restClient.responseSpec.toEntity(String.class)).thenReturn(serverErrorResponse);

        when(draftAccountTransactions
                 .updateStatus(publish, DraftAccountStatus.LEGACY_PENDING, draftAccountTransactions))
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
                BusinessUnitEntity.builder()
                    .businessUnitId((short)6)
                    .build())
            .timelineData(emptyTimelineData())
            .build();

        LegacyCreateDefendantAccountResponse responseBody = LegacyCreateDefendantAccountResponse.builder()
            .errorResponse("Something went wrong on the server.")
            .build();

        ParameterizedTypeReference typeRef = new ParameterizedTypeReference<LegacyCreateDefendantAccountResponse>(){};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(responseBody);

        ResponseEntity<String> serverErrorResponse =
            new ResponseEntity<>(responseBody.toXml(), HttpStatus.MOVED_PERMANENTLY);
        when(restClient.responseSpec.toEntity(String.class)).thenReturn(serverErrorResponse);

        when(draftAccountTransactions
                 .updateStatus(publish, DraftAccountStatus.LEGACY_PENDING, draftAccountTransactions))
            .thenReturn(publish);

        DraftAccountEntity published = legacyDraftAccountPublish.publishDefendantAccount(publish, buu);

        assertEquals(publish, published);
    }

    private String emptyTimelineData() {
        return new TimelineData().toJson();
    }

}
