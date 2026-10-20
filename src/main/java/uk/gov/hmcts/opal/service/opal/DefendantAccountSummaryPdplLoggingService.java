package uk.gov.hmcts.opal.service.opal;

import static uk.gov.hmcts.opal.dto.PdplIdentifierType.DEFENDANT_ACCOUNT;
import static uk.gov.hmcts.opal.dto.PdplIdentifierType.PARENT_GUARDIAN;
import static uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory.CONSULTATION;

import java.time.Clock;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.GetDefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

@Service
public class DefendantAccountSummaryPdplLoggingService extends AbstractPdplLoggingService {
    private static final String BUSINESS_IDENTIFIER = "View Defendant Account";
    private static final String QUEUE_FAILURE_MESSAGE =
        "Unable to queue PDPO log for viewing a defendant account summary";

    public DefendantAccountSummaryPdplLoggingService(LoggingService loggingService, Clock clock) {
        super(loggingService, clock);
    }

    public void logAtAGlance(GetDefendantAccountAtAGlanceResponse response, Long userId) {
        if (response.getPayload().getDebtorType() != null
            && response.getPayload().getPartyDetails() != null
            && !response.getPayload().getPartyDetails().getOrganisationFlag()) {
            logIndividualDebtorDetails(response.getPayload().getDefendantAccountId(),
                response.getPayload().getDebtorType().getValue(), userId);
        }
    }

    public void logHeaderSummary(DefendantAccountHeaderSummary response, Long userId) {
        if (response.getResponse().getDebtorType() != null
            && response.getResponse().getPartyDetails() != null
            && !response.getResponse().getPartyDetails().getOrganisationFlag()) {
            logIndividualDebtorDetails(response.getResponse().getDefendantAccountId(),
                response.getResponse().getDebtorType().getValue(), userId);
        }
    }

    private void logIndividualDebtorDetails(String defendantAccountId, String debtorType, Long userId) {
        switch (debtorType) {
            case "Defendant" -> logIndividualDebtorDetails(defendantAccountId, DEFENDANT_ACCOUNT, userId);
            case "Parent/Guardian" -> logIndividualDebtorDetails(defendantAccountId, PARENT_GUARDIAN, userId);
        }
    }

    private void logIndividualDebtorDetails(String defendantAccountId, PdplIdentifierType pdplIdentifierType,
                                            Long userId) {
        List<ParticipantIdentifier> participant = List.of(participant(defendantAccountId, pdplIdentifierType));
        logPdplOrThrow(BUSINESS_IDENTIFIER, CONSULTATION, participant, null, userId, QUEUE_FAILURE_MESSAGE);
    }
}
