package uk.gov.hmcts.opal.service.opal;

import static uk.gov.hmcts.opal.dto.PdplIdentifierType.OPAL_USER_ID;
import static uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory.CONSULTATION;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import uk.gov.hmcts.opal.dto.response.GetMinorCreditorHistoryResponse;
import uk.gov.hmcts.opal.generated.model.MinorCreditorHistoryItemHistory;
import uk.gov.hmcts.opal.generated.model.PostedDetailsCommon;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

@Service
public class MinorCreditorHistoryPdplLoggingService extends AbstractPdplLoggingService {

    private static final String BUSINESS_IDENTIFIER = "View Minor Creditor Account History Item";
    private static final String QUEUE_FAILURE_MESSAGE = "Unable to queue PDPO log for minor creditor account history";

    public MinorCreditorHistoryPdplLoggingService(LoggingService loggingService, Clock clock) {
        super(loggingService, clock);
    }

    public void logHistoryItems(UserStateV2 userState, GetMinorCreditorHistoryResponse response) {
        List<ParticipantIdentifier> individuals = getUniquePostedUsers(response);

        if (individuals.isEmpty()) {
            return;
        }

        logPdplOrThrow(BUSINESS_IDENTIFIER, CONSULTATION, individuals, null, userState.getUserId(),
            QUEUE_FAILURE_MESSAGE);
    }

    private List<ParticipantIdentifier> getUniquePostedUsers(GetMinorCreditorHistoryResponse response) {
        if (response == null || response.getPayload() == null || response.getPayload().getHistoryItems() == null) {
            return List.of();
        }

        return response.getPayload().getHistoryItems().stream()
            .filter(Objects::nonNull)
            .map(MinorCreditorHistoryItemHistory::getPostedDetails)
            .filter(Objects::nonNull)
            .map(PostedDetailsCommon::getPostedBy)
            .filter(StringUtils::isNotBlank)
            .distinct()
            .map(identifier -> participant(identifier, OPAL_USER_ID))
            .toList();
    }
}
