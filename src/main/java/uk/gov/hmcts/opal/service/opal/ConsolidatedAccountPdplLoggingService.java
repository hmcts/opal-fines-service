package uk.gov.hmcts.opal.service.opal;

import static uk.gov.hmcts.opal.dto.PdplIdentifierType.DEFENDANT;
import static uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory.CONSULTATION;

import java.time.Clock;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

@Service
public class ConsolidatedAccountPdplLoggingService extends AbstractPdplLoggingService {

    private static final String BUSINESS_IDENTIFIER = "View Consolidated Accounts - Individual";
    private static final String QUEUE_FAILURE_MESSAGE = "Unable to queue PDPO log for consolidated accounts";

    public ConsolidatedAccountPdplLoggingService(LoggingService loggingService, Clock clock) {
        super(loggingService, clock);
    }

    public void logIndividualChildAccounts(UserState userState, List<Long> individualChildAccountIds) {
        if (individualChildAccountIds.isEmpty()) {
            return;
        }

        List<ParticipantIdentifier> individuals = individualChildAccountIds.stream()
            .distinct()
            .map(id -> participant(id.toString(), DEFENDANT))
            .toList();

        logPdplOrThrow(BUSINESS_IDENTIFIER, CONSULTATION, individuals, null, userState.getUserId(),
            QUEUE_FAILURE_MESSAGE);
    }
}
