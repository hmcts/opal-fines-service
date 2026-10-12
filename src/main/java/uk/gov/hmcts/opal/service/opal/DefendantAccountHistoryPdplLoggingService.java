package uk.gov.hmcts.opal.service.opal;

import java.time.Clock;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryItem;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryResponse;
import uk.gov.hmcts.opal.exception.PdplLoggingException;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

@Service
@Slf4j(topic = "opal.pdplLoggingService")
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

        boolean queued;
        try {
            queued = logPdpl(BUSINESS_IDENTIFIER, PersonalDataProcessingCategory.CONSULTATION,
                individuals, null, userState);
        } catch (RuntimeException ex) {
            log.error(QUEUE_FAILURE_MESSAGE, ex);
            throw new PdplLoggingException(QUEUE_FAILURE_MESSAGE, ex);
        }

        if (!queued) {
            log.error(QUEUE_FAILURE_MESSAGE);
            throw new PdplLoggingException(QUEUE_FAILURE_MESSAGE);
        }
    }

    private List<ParticipantIdentifier> getUniquePostedUsers(DefendantAccountHistoryResponse response) {
        if (response == null || response.getHistoryItems() == null) {
            return List.of();
        }

        return response.getHistoryItems().stream()
            .filter(item -> item != null && item.getPostedDetails() != null)
            .map(DefendantAccountHistoryItem::getPostedDetails)
            .map(postedDetails -> postedDetails.getPostedBy())
            .filter(identifier -> identifier != null && !identifier.isBlank())
            .distinct()
            .map(identifier -> ParticipantIdentifier.builder()
                .identifier(identifier)
                .type(PdplIdentifierType.OPAL_USER_ID)
                .build())
            .toList();
    }
}
