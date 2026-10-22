package uk.gov.hmcts.opal.service.opal;

import java.time.Clock;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.generated.model.DefendantAccountParty;
import uk.gov.hmcts.opal.generated.model.DefendantAccountParty.DefendantAccountPartyTypeEnum;
import uk.gov.hmcts.opal.generated.model.PartyResponseDefendantAccount;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

@Service
public class DefendantAccountPartyPdplLoggingService extends AbstractPdplLoggingService {

    private static final String ADD_PARENT_GUARDIAN_BUSINESS_IDENTIFIER = "Add Parent/Guardian";
    private static final String VIEW_DEFENDANT_BUSINESS_IDENTIFIER = "View Defendant";
    private static final String VIEW_PARENT_GUARDIAN_BUSINESS_IDENTIFIER = "View Parent/Guardian";
    private static final String VIEW_PARTY_QUEUE_FAILURE_MESSAGE = "Unable to queue PDPO log for viewing a party";

    public DefendantAccountPartyPdplLoggingService(LoggingService loggingService, Clock clock) {
        super(loggingService, clock);
    }

    public boolean logAddedParentGuardian(String partyId, UserState userState) {
        ParticipantIdentifier parentGuardian = ParticipantIdentifier.builder()
            .identifier(partyId)
            .type(PdplIdentifierType.PARENT_GUARDIAN)
            .build();

        return logPdpl(ADD_PARENT_GUARDIAN_BUSINESS_IDENTIFIER, PersonalDataProcessingCategory.COLLECTION,
            List.of(parentGuardian), null, userState);
    }

    public void queuePdpoForViewedParty(PartyResponseDefendantAccount response, UserState userState) {
        DefendantAccountParty party = response.getDefendantAccountParty();
        DefendantAccountPartyTypeEnum partyType = party.getDefendantAccountPartyType();

        switch (partyType) {
            case DEFENDANT -> logViewedDefendant(requirePartyId(response, "Defendant"), userState);
            case PARENT_GUARDIAN -> logViewedParentGuardian(
                requirePartyId(response, "Parent/Guardian"), userState);
        }
    }

    public void logViewedDefendant(String partyId, UserState userState) {
        logViewedParty(VIEW_DEFENDANT_BUSINESS_IDENTIFIER, partyId, PdplIdentifierType.DEFENDANT_ACCOUNT, userState);
    }

    public void logViewedParentGuardian(String partyId, UserState userState) {
        logViewedParty(VIEW_PARENT_GUARDIAN_BUSINESS_IDENTIFIER, partyId, PdplIdentifierType.PARENT_GUARDIAN,
            userState);
    }

    private void logViewedParty(String businessIdentifier, String partyId, PdplIdentifierType partyType,
        UserState userState) {
        logPdplOrThrow(businessIdentifier, PersonalDataProcessingCategory.CONSULTATION,
            List.of(participant(partyId, partyType)),
            null,
            userState.getUserId(), VIEW_PARTY_QUEUE_FAILURE_MESSAGE);
    }

    private String requirePartyId(PartyResponseDefendantAccount response, String partyType) {
        String partyId = response.getDefendantAccountParty().getPartyDetails().getPartyId();
        if (partyId == null || partyId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Unable to record required " + partyType + " PDPO log");
        }

        return partyId;
    }

    public String requireAddedPartyId(PartyResponseDefendantAccount response) {
        return requirePartyId(response, "Parent/Guardian");
    }
}
