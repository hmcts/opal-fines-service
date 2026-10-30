package uk.gov.hmcts.opal.service.opal;

import static uk.gov.hmcts.opal.dto.PdplIdentifierType.PARENT_GUARDIAN;

import java.time.Clock;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.generated.model.DefendantAccountParty;
import uk.gov.hmcts.opal.generated.model.DefendantAccountParty.DefendantAccountPartyTypeEnum;
import uk.gov.hmcts.opal.generated.model.PartyResponseDefendantAccount;
import uk.gov.hmcts.opal.generated.model.RemoveDefendantAccountPartyRequestDefendantAccount;
import uk.gov.hmcts.opal.generated.model.RemoveDefendantAccountPartyResponseDefendantAccount;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

@Service
public class DefendantAccountPartyPdplLoggingService extends AbstractPdplLoggingService {

    private static final String ADD_PARENT_GUARDIAN_BUSINESS_IDENTIFIER = "Add Parent/Guardian";
    private static final String VIEW_DEFENDANT_BUSINESS_IDENTIFIER = "View Defendant";
    private static final String VIEW_PARENT_GUARDIAN_BUSINESS_IDENTIFIER = "View Parent/Guardian";
    private static final String VIEW_PARTY_QUEUE_FAILURE_MESSAGE = "Unable to queue PDPO log for viewing a party";
    private static final String REMOVE_PARENT_GUARDIAN_BUSINESS_IDENTIFIER = "Remove Parent/Guardian";
    private static final String REMOVE_PARENT_GUARDIAN_FAILURE =
        "Unable to queue PDPO log for removed Parent/Guardian";

    public DefendantAccountPartyPdplLoggingService(LoggingService loggingService, Clock clock) {
        super(loggingService, clock);
    }

    public boolean logAddedParentGuardian(String partyId, UserStateV2 userState) {
        ParticipantIdentifier parentGuardian = participant(partyId, PARENT_GUARDIAN);

        return logPdpl(ADD_PARENT_GUARDIAN_BUSINESS_IDENTIFIER, PersonalDataProcessingCategory.COLLECTION,
            List.of(parentGuardian), null, userState);
    }

    public void queuePdpoForViewedParty(PartyResponseDefendantAccount response, UserStateV2 userState) {
        DefendantAccountParty party = response.getDefendantAccountParty();
        DefendantAccountPartyTypeEnum partyType = party.getDefendantAccountPartyType();

        switch (partyType) {
            case DEFENDANT -> logViewedDefendant(requirePartyId(response, "Defendant"), userState);
            case PARENT_GUARDIAN -> logViewedParentGuardian(
                requirePartyId(response, "Parent/Guardian"), userState);
        }
    }

    public void logViewedDefendant(String partyId, UserStateV2 userState) {
        logViewedParty(VIEW_DEFENDANT_BUSINESS_IDENTIFIER, partyId, PdplIdentifierType.DEFENDANT_ACCOUNT, userState);
    }

    public void logViewedParentGuardian(String partyId, UserStateV2 userState) {
        logViewedParty(VIEW_PARENT_GUARDIAN_BUSINESS_IDENTIFIER, partyId, PdplIdentifierType.PARENT_GUARDIAN,
            userState);
    }

    private void logViewedParty(String businessIdentifier, String partyId, PdplIdentifierType partyType,
        UserStateV2 userState) {
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

    public void logRemovedParentGuardian(Long defendantAccountPartyId,
        RemoveDefendantAccountPartyRequestDefendantAccount request,
        RemoveDefendantAccountPartyResponseDefendantAccount response,
        UserStateV2 userState) {
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
