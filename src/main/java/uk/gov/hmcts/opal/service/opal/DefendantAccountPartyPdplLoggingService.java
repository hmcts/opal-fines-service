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
    private static final String QUEUE_FAILURE_MESSAGE = "Unable to queue PDPO log for defendant account party";

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

    public void logReplacement(Long partyId, AssociationType associationType, boolean wasOrganisation,
                               boolean organisation, List<Long> removedParentGuardianIds, UserState userState) {
        if (associationType == AssociationType.PARENT_GUARDIAN) {
            logPdplOrThrow("Update Parent/Guardian", PersonalDataProcessingCategory.ALTERATION,
                List.of(participant(partyId.toString(), PdplIdentifierType.PARENT_GUARDIAN)),
                null, userState.getUserId(), QUEUE_FAILURE_MESSAGE);
        } else if (associationType == AssociationType.DEFENDANT) {
            String businessIdentifier = "Update Defendant";
            PersonalDataProcessingCategory category = PersonalDataProcessingCategory.ALTERATION;
            if (wasOrganisation && !organisation) {
                businessIdentifier = "Convert Company to Individual";
                category = PersonalDataProcessingCategory.COLLECTION;
            } else if (!wasOrganisation && organisation) {
                businessIdentifier = "Convert Individual to Company";
                category = PersonalDataProcessingCategory.ERASURE;
            }
            logPdplOrThrow(businessIdentifier, category,
                List.of(participant(partyId.toString(), PdplIdentifierType.DEFENDANT)),
                null, userState.getUserId(), QUEUE_FAILURE_MESSAGE);
        }

        if (!removedParentGuardianIds.isEmpty()) {
            List<ParticipantIdentifier> individuals = removedParentGuardianIds.stream()
                .distinct()
                .map(id -> participant(id.toString(), PdplIdentifierType.PARENT_GUARDIAN))
                .toList();
            logPdplOrThrow("Remove Parent/Guardian", PersonalDataProcessingCategory.ERASURE,
                individuals, null, userState.getUserId(), QUEUE_FAILURE_MESSAGE);
        }
    }
}
