package uk.gov.hmcts.opal.service.opal;

import static uk.gov.hmcts.opal.dto.PdplIdentifierType.MINOR_CREDITOR;
import static uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory.CONSULTATION;

import java.time.Clock;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

@Service
public class MinorCreditorSummaryPdplLoggingService extends AbstractPdplLoggingService {

    private static final String VIEW_MINOR_CREDITOR_ACCOUNT = "View Minor Creditor Account - Individual";

    private static final String QUEUE_FAILURE_MESSAGE = "Unable to queue PDPO log for minor creditor account";

    public MinorCreditorSummaryPdplLoggingService(LoggingService loggingService, Clock clock) {
        super(loggingService, clock);
    }

    public void logIndividualMinorCreditorSummary(Long minorCreditorAccountId, UserState userState,
        MinorCreditorAccountHeaderSummaryResponse response) {
        if (response == null || response.getParty() == null
            || !Boolean.FALSE.equals(response.getParty().getOrganisationFlag())) {
            return;
        }

        ParticipantIdentifier participant = ParticipantIdentifier.builder()
            .identifier(minorCreditorAccountId.toString())
            .type(MINOR_CREDITOR)
            .build();

        logPdplOrThrow(VIEW_MINOR_CREDITOR_ACCOUNT, CONSULTATION, List.of(participant), null, userState.getUserId(),
            QUEUE_FAILURE_MESSAGE);
    }
}
