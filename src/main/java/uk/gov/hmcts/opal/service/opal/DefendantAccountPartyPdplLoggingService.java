package uk.gov.hmcts.opal.service.opal;

import java.time.Clock;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.entity.defendantaccount.AssociationType;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

@Service
public class DefendantAccountPartyPdplLoggingService extends AbstractPdplLoggingService {

    private static final String ADD_PARENT_GUARDIAN_BUSINESS_IDENTIFIER = "Add Parent/Guardian";
    private static final String UPDATE_DEFENDANT_BUSINESS_IDENTIFIER = "Update Defendant";
    private static final String UPDATE_PARENT_GUARDIAN_BUSINESS_IDENTIFIER = "Update Parent/Guardian";
    private static final String CONVERT_COMPANY_TO_INDIVIDUAL_BUSINESS_IDENTIFIER = "Convert Company to Individual";
    private static final String CONVERT_INDIVIDUAL_TO_COMPANY_BUSINESS_IDENTIFIER = "Convert Individual to Company";
    private static final String REMOVE_PARENT_GUARDIAN_BUSINESS_IDENTIFIER = "Remove Parent/Guardian";
    private static final String QUEUE_FAILURE_MESSAGE = "Unable to queue PDPO log for defendant account party";

    public DefendantAccountPartyPdplLoggingService(LoggingService loggingService, Clock clock) {
        super(loggingService, clock);
    }

    public boolean logAddedParentGuardian(String partyId, UserState userState) {
        return logPdpl(ADD_PARENT_GUARDIAN_BUSINESS_IDENTIFIER, PersonalDataProcessingCategory.COLLECTION,
            List.of(participant(partyId, PdplIdentifierType.PARENT_GUARDIAN)), null, userState);
    }

    public void logReplacement(Long partyId, AssociationType associationType, boolean wasOrganisation,
        boolean organisation, List<Long> removedParentGuardianIds, UserState userState) {
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

        if (!removedParentGuardianIds.isEmpty()) {
            List<ParticipantIdentifier> individuals = removedParentGuardianIds.stream()
                .distinct()
                .map(id -> participant(id.toString(), PdplIdentifierType.PARENT_GUARDIAN))
                .toList();
            logPdplOrThrow(REMOVE_PARENT_GUARDIAN_BUSINESS_IDENTIFIER, PersonalDataProcessingCategory.ERASURE,
                individuals, null, userState, QUEUE_FAILURE_MESSAGE);
        }
    }

    private void logParty(String businessIdentifier, PersonalDataProcessingCategory category, String partyId,
        PdplIdentifierType partyType, UserState userState) {
        logPdplOrThrow(businessIdentifier, category, List.of(participant(partyId, partyType)), null,
            userState, QUEUE_FAILURE_MESSAGE);
    }
}
