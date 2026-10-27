package uk.gov.hmcts.opal.service.opal;

import static uk.gov.hmcts.opal.dto.PdplIdentifierType.OPAL_USER_ID;
import static uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory.CONSULTATION;

import java.time.Clock;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.PostedDetails;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryItem;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryResponse;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

@Service
public class DefendantAccountHistoryPdplLoggingService extends AbstractPdplLoggingService {

    private static final String BUSINESS_IDENTIFIER = "View Defendant Account History Item";
    private static final String QUEUE_FAILURE_MESSAGE = "Unable to queue PDPO log for defendant account history";

    public DefendantAccountHistoryPdplLoggingService(LoggingService loggingService, Clock clock) {
        super(loggingService, clock);
    }

    public void logHistoryItems(UserState userState, DefendantAccountHistoryResponse response) {
        List<ParticipantIdentifier> individuals = getUniquePostedUsers(response);
        if (individuals.isEmpty()) {
            return;
        }

        logPdplOrThrow(BUSINESS_IDENTIFIER, CONSULTATION, individuals, null, userState.getUserId(),
            QUEUE_FAILURE_MESSAGE);
    }

    private List<ParticipantIdentifier> getUniquePostedUsers(DefendantAccountHistoryResponse response) {
        if (response == null || response.getHistoryItems() == null) {
            return List.of();
        }

        return response.getHistoryItems().stream()
            .filter(item -> item != null && item.getPostedDetails() != null)
            .map(DefendantAccountHistoryItem::getPostedDetails)
            .map(PostedDetails::getPostedBy)
            .filter(StringUtils::isNotBlank)
            .distinct()
            .map(identifier -> participant(identifier, OPAL_USER_ID))
            .toList();
    }
}
