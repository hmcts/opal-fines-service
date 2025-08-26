package uk.gov.hmcts.opal.service.legacy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.dto.legacy.LegacyCreateDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyCreateDefendantAccountResponse;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.entity.draft.TimelineData;
import uk.gov.hmcts.opal.exception.JsonSchemaValidationException;
import uk.gov.hmcts.opal.service.iface.DraftAccountPublishInterface;
import uk.gov.hmcts.opal.service.legacy.GatewayService.Response;
import uk.gov.hmcts.opal.service.opal.jpa.DraftAccountTransactions;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.LegacyDraftAccountPublish")
public class LegacyDraftAccountPublish implements DraftAccountPublishInterface {

    public static final String CREATE_DEFENDANT_ACCOUNT = "createAccount";

    private final GatewayService gatewayService;
    private final DraftAccountTransactions draftAccountTransactions;

    @Override
    public DraftAccountEntity publishDefendantAccount(DraftAccountEntity publishEntity, BusinessUnitUser unitUser) {
        log.info(":publishDefendantAccount: ");

        CompletableFuture<Response<LegacyCreateDefendantAccountResponse>> future = gatewayService.postToGatewayAsync(
            CREATE_DEFENDANT_ACCOUNT, LegacyCreateDefendantAccountResponse.class,
            createDefendantAccountRequest(publishEntity, unitUser), null);

        publishEntity = draftAccountTransactions
            .updateStatus(publishEntity, DraftAccountStatus.LEGACY_PENDING, draftAccountTransactions);

        try {
            Response<LegacyCreateDefendantAccountResponse> response = future.get();

            if (response.isError()) {
                log.error(":publishDefendantAccount: Legacy Gateway response: HTTP Response Code: {}", response.code);
                if (response.isException()) {
                    log.error(":publishDefendantAccount:", response.exception);
                } else if (response.isLegacyFailure()) {
                    log.error(":publishDefendantAccount: Legacy Gateway: body: \n{}", response.body);
                    LegacyCreateDefendantAccountResponse responseEntity = response.responseEntity;
                    log.error(":publishDefendantAccount: Legacy Gateway: entity: \n{}", responseEntity.toXml());
                    String errorResponse = responseEntity.getErrorResponse();

                    TimelineData timelineData = new TimelineData(publishEntity.getTimelineData());
                    timelineData.insertEntry(
                        unitUser.getBusinessUnitUserId(), DraftAccountStatus.PUBLISHING_FAILED.getLabel(),
                        LocalDate.now(), errorResponse
                    );
                    publishEntity.setTimelineData(timelineData.toJson());
                    publishEntity.setStatusMessage(errorResponse);

                    publishEntity = draftAccountTransactions
                        .updateStatus(publishEntity, DraftAccountStatus.PUBLISHING_FAILED, draftAccountTransactions);
                }
            } else if (response.isSuccessful()) {
                log.info(":publishDefendantAccount: Legacy Gateway response: Success.");
                publishEntity.setAccountId(response.responseEntity.getDefendantAccountId());
                publishEntity.setAccountNumber(response.responseEntity.getDefendantAccountNumber());
                publishEntity = draftAccountTransactions
                    .updateStatus(publishEntity, DraftAccountStatus.PUBLISHED, draftAccountTransactions);
            }
        } catch (InterruptedException e) {
            log.error(":publishDefendantAccount: problem with call to Legacy: {}", e.getMessage());
            log.error(":publishDefendantAccount:", e);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            log.error(":publishDefendantAccount: problem with call to Legacy: {}", e.getMessage());
            log.error(":publishDefendantAccount:", e);
            throw new RuntimeException(e);
        }
        return publishEntity;
    }

    public static LegacyCreateDefendantAccountRequest createDefendantAccountRequest(DraftAccountEntity entity,
                                                                                    BusinessUnitUser unitUser) {
        String accountJson = entity.getAccount();
        JsonNode account;
        try {
            account = (accountJson == null || accountJson.isBlank())
                ? null
                : ToJsonString.toJsonNode(accountJson);
        } catch (JsonProcessingException e) {
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
