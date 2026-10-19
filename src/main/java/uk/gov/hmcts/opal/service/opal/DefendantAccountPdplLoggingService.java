package uk.gov.hmcts.opal.service.opal;

import static uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory.CONSULTATION;

import java.time.Clock;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.GetDefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

@Service
public class DefendantAccountPdplLoggingService extends AbstractPdplLoggingService {
    private static final String BUSINESS_IDENTIFIER = "View Defendant Account";
    private static final String QUEUE_FAILURE_MESSAGE = "Unable to queue PDPO log for viewing a defendant account";
    private static final List<String> DEBTOR_TYPES_TO_LOG = List.of("Defendant", "Parent/Guardian");

    public DefendantAccountPdplLoggingService(LoggingService loggingService, Clock clock) {
        super(loggingService, clock);
    }

    public void logAtAGlance(GetDefendantAccountAtAGlanceResponse response, Long userId) {
        logIndividualDebtorDetails(response.getPayload().getDefendantAccountId(),
            response.getPayload().getDebtorType().getValue(), userId);
    }

    public void logHeaderSummary(DefendantAccountHeaderSummary response, Long userId) {
        logIndividualDebtorDetails(response.getResponse().getDefendantAccountId(),
            response.getResponse().getDebtorType().getValue(), userId);
    }

    private void logIndividualDebtorDetails(String defendantAccountId, String debtorType, Long userId) {
        if (DEBTOR_TYPES_TO_LOG.contains(debtorType)) {
            PdplIdentifierType pdplIdentifierType = switch (debtorType) {
                case "Defendant" -> PdplIdentifierType.DEFENDANT_ACCOUNT;
                case "Parent/Guardian" -> PdplIdentifierType.PARENT_GUARDIAN;
                default -> null;
            };
            List<ParticipantIdentifier> participant = List.of(participant(defendantAccountId, pdplIdentifierType));
            logPdplOrThrow(BUSINESS_IDENTIFIER, CONSULTATION, participant, null, userId,
                QUEUE_FAILURE_MESSAGE);
        }
    }
}
