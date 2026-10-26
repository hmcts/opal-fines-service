package uk.gov.hmcts.opal.service.opal;

import static uk.gov.hmcts.opal.dto.PdplIdentifierType.PARENT_GUARDIAN;

import java.time.Clock;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.generated.model.RemoveDefendantAccountPartyRequestDefendantAccount;
import uk.gov.hmcts.opal.generated.model.RemoveDefendantAccountPartyResponseDefendantAccount;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

@Service
public class DefendantAccountPartyPdplLoggingService extends AbstractPdplLoggingService {

    private static final String ADD_PARENT_GUARDIAN_BUSINESS_IDENTIFIER = "Add Parent/Guardian";
    private static final String REMOVE_PARENT_GUARDIAN_BUSINESS_IDENTIFIER = "Remove Parent/Guardian";
    private static final String REMOVE_PARENT_GUARDIAN_FAILURE =
        "Unable to queue PDPO log for removed Parent/Guardian";

    public DefendantAccountPartyPdplLoggingService(LoggingService loggingService, Clock clock) {
        super(loggingService, clock);
    }

    public boolean logAddedParentGuardian(String partyId, UserState userState) {
        ParticipantIdentifier parentGuardian = participant(partyId, PARENT_GUARDIAN);

        return logPdpl(ADD_PARENT_GUARDIAN_BUSINESS_IDENTIFIER, PersonalDataProcessingCategory.COLLECTION,
            List.of(parentGuardian), null, userState);
    }

    public void logRemovedParentGuardian(Long defendantAccountPartyId,
        RemoveDefendantAccountPartyRequestDefendantAccount request,
        RemoveDefendantAccountPartyResponseDefendantAccount response,
        UserState userState) {
        if (response.getDefendantAccountPartyId() == null || response.getDefendantAccountPartyId().isBlank()) {
            return;
        }

        String partyId;
        if (request.getPartyDetails() == null) {
            partyId = String.valueOf(defendantAccountPartyId);
        } else {
            partyId = request.getPartyDetails().getPartyId();
        }
        ParticipantIdentifier parentGuardian = participant(partyId, PARENT_GUARDIAN);

        logPdplOrThrow(REMOVE_PARENT_GUARDIAN_BUSINESS_IDENTIFIER, PersonalDataProcessingCategory.ERASURE,
            List.of(parentGuardian), null, userState.getUserId(), REMOVE_PARENT_GUARDIAN_FAILURE);
    }
}
