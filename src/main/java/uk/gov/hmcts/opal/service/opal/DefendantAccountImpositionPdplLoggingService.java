package uk.gov.hmcts.opal.service.opal;

import static uk.gov.hmcts.opal.dto.PdplIdentifierType.MINOR_CREDITOR;
import static uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory.CONSULTATION;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.GetDefendantAccountImpositionsResponse;
import uk.gov.hmcts.opal.generated.model.CreditorSummaryCommon;
import uk.gov.hmcts.opal.generated.model.DefendantAccountImpositionCommon;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

@Service
public class DefendantAccountImpositionPdplLoggingService extends AbstractPdplLoggingService {

    private static final String BUSINESS_IDENTIFIER = "View Defendant Impositions (Individual Minor Creditor)";
    private static final String QUEUE_FAILURE_MESSAGE = "Unable to queue PDPO log for defendant impositions";

    public DefendantAccountImpositionPdplLoggingService(LoggingService loggingService, Clock clock) {
        super(loggingService, clock);
    }

    public void logIndividualMinorCreditors(UserState userState, GetDefendantAccountImpositionsResponse response) {
        List<ParticipantIdentifier> individuals = getIndividualMinorCreditors(response);
        if (individuals.isEmpty()) {
            return;
        }

        logPdplOrThrow(BUSINESS_IDENTIFIER, CONSULTATION, individuals, null, userState.getUserId(),
            QUEUE_FAILURE_MESSAGE);
    }

    private List<ParticipantIdentifier> getIndividualMinorCreditors(
        GetDefendantAccountImpositionsResponse response) {

        if (response == null || response.getPayload() == null || response.getPayload().getImpositions() == null) {
            return List.of();
        }

        return response.getPayload().getImpositions().stream()
            .filter(imposition -> imposition != null && imposition.getCreditor() != null)
            .map(DefendantAccountImpositionCommon::getCreditor)
            .filter(creditor -> Boolean.FALSE.equals(creditor.getMinorCreditorOrganisationFlag()))
            .map(CreditorSummaryCommon::getCreditorAccountId)
            .filter(Objects::nonNull)
            .distinct()
            .map(identifier -> participant(identifier.toString(), MINOR_CREDITOR))
            .toList();
    }
}
