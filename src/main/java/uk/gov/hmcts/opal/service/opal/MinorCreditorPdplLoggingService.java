package uk.gov.hmcts.opal.service.opal;

import static uk.gov.hmcts.opal.dto.PdplIdentifierType.MINOR_CREDITOR;
import static uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory.CONSULTATION;

import java.time.Clock;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

@Service
public class MinorCreditorPdplLoggingService extends AbstractPdplLoggingService {

    private static final String BUSINESS_IDENTIFIER = "View Minor Creditor";
    private static final String UPDATE_BUSINESS_IDENTIFIER = "Update Minor Creditor";
    private static final String QUEUE_FAILURE_MESSAGE = "Unable to queue PDPO log for minor creditor";
    private static final String UPDATE_QUEUE_FAILURE_MESSAGE = "Unable to queue PDPO log for minor creditor update";

    public MinorCreditorPdplLoggingService(LoggingService loggingService, Clock clock) {
        super(loggingService, clock);
    }

    public void logIndividualMinorCreditor(UserState userState, MinorCreditorAccountResponse response) {
        if (!isIndividualMinorCreditor(response)) {
            return;
        }

        ParticipantIdentifier minorCreditor = participant(response.getCreditorAccountId().toString(),
            MINOR_CREDITOR);
        logPdplOrThrow(BUSINESS_IDENTIFIER, CONSULTATION, List.of(minorCreditor), null, userState.getUserId(),
            QUEUE_FAILURE_MESSAGE);
    }

    public void logUpdate(Long creditorAccountId, UserState userState) {
        logPdplOrThrow(UPDATE_BUSINESS_IDENTIFIER, PersonalDataProcessingCategory.ALTERATION,
            List.of(participant(creditorAccountId.toString(), PdplIdentifierType.MINOR_CREDITOR)), null,
            userState, UPDATE_QUEUE_FAILURE_MESSAGE);
    }

    private boolean isIndividualMinorCreditor(MinorCreditorAccountResponse response) {
        return response != null
            && response.getPartyDetails() != null
            && Boolean.FALSE.equals(response.getPartyDetails().getOrganisationFlag());
    }
}
