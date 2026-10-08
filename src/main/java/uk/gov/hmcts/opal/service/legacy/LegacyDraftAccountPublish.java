package uk.gov.hmcts.opal.service.legacy;

import tools.jackson.core.JacksonException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService.Response;
import uk.gov.hmcts.opal.common.logging.LogUtil;
import uk.gov.hmcts.opal.common.dto.ToJsonString;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUserV2;
import uk.gov.hmcts.opal.dto.legacy.LegacyCreateDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyCreateDefendantAccountResponse;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.entity.draft.TimelineData;
import uk.gov.hmcts.opal.exception.JsonSchemaValidationException;
import uk.gov.hmcts.opal.service.iface.DraftAccountPublishInterface;
import uk.gov.hmcts.opal.service.opal.jpa.DraftAccountTransactional;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.LegacyDraftAccountPublish")
public class LegacyDraftAccountPublish implements DraftAccountPublishInterface {

    public static final String CREATE_DEFENDANT_ACCOUNT = "createAccount";
    private static final String PUBLISH_DEFENDANT_ACCOUNT_LOG_PREFIX = ":publishDefendantAccount:";

    public static final String ERROR_MESSAGE_TEMPLATE =
        "An error was encountered during publication of the account, please contact the service desk. Error code: [%s]";

    private final GatewayService gatewayService;
    private final DraftAccountTransactional draftAccountTransactional;

    @Override
    public DraftAccountEntity publishDefendantAccount(DraftAccountEntity publishEntity, BusinessUnitUserV2 unitUser) {
        log.info(PUBLISH_DEFENDANT_ACCOUNT_LOG_PREFIX + " ");

        CompletableFuture<Response<LegacyCreateDefendantAccountResponse>> future = gatewayService.postToGatewayAsync(
            CREATE_DEFENDANT_ACCOUNT, LegacyCreateDefendantAccountResponse.class,
            createDefendantAccountRequest(publishEntity, unitUser), null);

        publishEntity = draftAccountTransactional
            .updateStatus(publishEntity, DraftAccountStatus.LEGACY_PENDING, draftAccountTransactional);

        try {
            Response<LegacyCreateDefendantAccountResponse> response = future.get();

            log.error(PUBLISH_DEFENDANT_ACCOUNT_LOG_PREFIX + " 1: {}", response.isException());
            log.error(PUBLISH_DEFENDANT_ACCOUNT_LOG_PREFIX + " 2: {}", response.code.isError());
            log.error(PUBLISH_DEFENDANT_ACCOUNT_LOG_PREFIX + " 3: {}", response.hasErrorResponse());
            if (response.isError()) {
                log.error(PUBLISH_DEFENDANT_ACCOUNT_LOG_PREFIX
                    + " Legacy Gateway response: HTTP Response Code: {}", response.code);
                if (response.isException()) {
                    log.error(PUBLISH_DEFENDANT_ACCOUNT_LOG_PREFIX, response.exception);
                } else if (response.hasErrorResponse()) {
                    log.error(PUBLISH_DEFENDANT_ACCOUNT_LOG_PREFIX + " Legacy Gateway: body: \n{}", response.body);
                    LegacyCreateDefendantAccountResponse responseEntity = response.responseEntity;
                    log.error(PUBLISH_DEFENDANT_ACCOUNT_LOG_PREFIX
                        + " Legacy Gateway: entity: \n{}", responseEntity.toXml());

                    String errorMessage = String.format(ERROR_MESSAGE_TEMPLATE, LogUtil.getOrCreateOpalOperationId());

                    TimelineData timelineData = new TimelineData(publishEntity.getTimelineData());
                    timelineData.insertEntry(
                        unitUser.getBusinessUnitUserId(), DraftAccountStatus.PUBLISHING_FAILED.getLabel(),
                        LocalDate.now(), errorMessage
                    );
                    publishEntity.setTimelineData(timelineData.toJson());
                    publishEntity.setStatusMessage(errorMessage);

                    publishEntity = draftAccountTransactional
                        .updateStatus(publishEntity, DraftAccountStatus.PUBLISHING_FAILED, draftAccountTransactional);
                } else {
                    log.warn(PUBLISH_DEFENDANT_ACCOUNT_LOG_PREFIX + " Unexpected Legacy Gateway response");
                }
            } else if (response.isSuccessful()) {
                log.info(PUBLISH_DEFENDANT_ACCOUNT_LOG_PREFIX + " Legacy Gateway response: Success.");
                publishEntity.setAccountId(response.responseEntity.getDefendantAccountId());
                publishEntity.setAccountNumber(response.responseEntity.getDefendantAccountNumber());
                publishEntity = draftAccountTransactional
                    .updateStatus(publishEntity, DraftAccountStatus.PUBLISHED, draftAccountTransactional);
            }
        } catch (InterruptedException e) {
            log.error(PUBLISH_DEFENDANT_ACCOUNT_LOG_PREFIX + " problem with call to Legacy: {}", e.getMessage());
            log.error(PUBLISH_DEFENDANT_ACCOUNT_LOG_PREFIX, e);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            log.error(PUBLISH_DEFENDANT_ACCOUNT_LOG_PREFIX + " problem with call to Legacy: {}", e.getMessage());
            log.error(PUBLISH_DEFENDANT_ACCOUNT_LOG_PREFIX, e);
            throw new RuntimeException(e);
        }
        return publishEntity;
    }

    public static LegacyCreateDefendantAccountRequest createDefendantAccountRequest(DraftAccountEntity entity,
                                                                                    BusinessUnitUserV2 unitUser) {
        String accountJson = entity.getAccount();
        Object account;
        try {
            account = (accountJson == null || accountJson.isBlank())
                ? null
                : ToJsonString.getObjectMapper().readValue(accountJson, Object.class);
        } catch (JacksonException e) {
            throw new JsonSchemaValidationException(
                "Failed to parse account JSON: " + e.getMessage(), e
            );
        }

        return LegacyCreateDefendantAccountRequest.builder()
            .businessUnitId(entity.getBusinessUnit().getBusinessUnitId())
            .businessUnitUserId(unitUser.getBusinessUnitUserId())
            .defendantAccount(account)
            .build();
    }
}
