package uk.gov.hmcts.opal.service.opal;

import static uk.gov.hmcts.opal.dto.PdplIdentifierType.PARENT_GUARDIAN;

import java.time.Clock;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.entity.defendantaccount.AssociationType;
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
    private static final String UPDATE_DEFENDANT_BUSINESS_IDENTIFIER = "Update Defendant";
    private static final String UPDATE_PARENT_GUARDIAN_BUSINESS_IDENTIFIER = "Update Parent/Guardian";
    private static final String CONVERT_COMPANY_TO_INDIVIDUAL_BUSINESS_IDENTIFIER = "Convert Company to Individual";
    private static final String CONVERT_INDIVIDUAL_TO_COMPANY_BUSINESS_IDENTIFIER = "Convert Individual to Company";
    private static final String REMOVE_PARENT_GUARDIAN_BUSINESS_IDENTIFIER = "Remove Parent/Guardian";
    private static final String REMOVE_PARENT_GUARDIAN_FAILURE =
        "Unable to queue PDPO log for removed Parent/Guardian";
    private static final String QUEUE_FAILURE_MESSAGE = "Unable to queue PDPO log for defendant account party";

    public DefendantAccountPartyPdplLoggingService(LoggingService loggingService, Clock clock) {
        super(loggingService, clock);
    }

    public boolean logAddedParentGuardian(String partyId, UserState userState) {
        ParticipantIdentifier parentGuardian = participant(partyId, PARENT_GUARDIAN);

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

    public void logReplacement(Long partyId, AssociationType associationType, boolean wasOrganisation,
        boolean organisation, List<Long> removedParentGuardianIds, UserState userState) {

        logReplacementOperation(partyId, associationType, wasOrganisation, organisation, userState);

        if (!removedParentGuardianIds.isEmpty()) {
            List<ParticipantIdentifier> individuals = removedParentGuardianIds.stream()
                .distinct()
                .map(id -> participant(id.toString(), PdplIdentifierType.PARENT_GUARDIAN))
                .toList();
            logPdplOrThrow(REMOVE_PARENT_GUARDIAN_BUSINESS_IDENTIFIER, PersonalDataProcessingCategory.ERASURE,
                individuals, null, userState, QUEUE_FAILURE_MESSAGE);
        }
    }

    private void logReplacementOperation(Long partyId, AssociationType associationType, boolean wasOrganisation,
        boolean organisation, UserState userState) {

        if (associationType == AssociationType.PARENT_GUARDIAN) {
            logParty(UPDATE_PARENT_GUARDIAN_BUSINESS_IDENTIFIER, PersonalDataProcessingCategory.ALTERATION,
                partyId.toString(), PdplIdentifierType.PARENT_GUARDIAN, userState);
        } else if (associationType == AssociationType.DEFENDANT) {
            String businessIdentifier = UPDATE_DEFENDANT_BUSINESS_IDENTIFIER;
            PersonalDataProcessingCategory category = PersonalDataProcessingCategory.ALTERATION;
            if (wasOrganisation && !organisation) {
                businessIdentifier = CONVERT_COMPANY_TO_INDIVIDUAL_BUSINESS_IDENTIFIER;
                category = PersonalDataProcessingCategory.COLLECTION;
            } else if (!wasOrganisation && organisation) {
                businessIdentifier = CONVERT_INDIVIDUAL_TO_COMPANY_BUSINESS_IDENTIFIER;
                category = PersonalDataProcessingCategory.ERASURE;
            }
            logParty(businessIdentifier, category, partyId.toString(), PdplIdentifierType.DEFENDANT_ACCOUNT, userState);
        }
    }

    private void logParty(String businessIdentifier, PersonalDataProcessingCategory category, String partyId,
        PdplIdentifierType partyType, UserState userState) {
        logPdplOrThrow(businessIdentifier, category, List.of(participant(partyId, partyType)), null,
            userState, QUEUE_FAILURE_MESSAGE);
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
