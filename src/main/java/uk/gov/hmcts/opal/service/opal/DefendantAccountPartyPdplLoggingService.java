package uk.gov.hmcts.opal.service.opal;

import java.time.Clock;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

@Service
public class DefendantAccountPartyPdplLoggingService extends AbstractPdplLoggingService {

    private static final String ADD_PARENT_GUARDIAN_BUSINESS_IDENTIFIER = "Add Parent/Guardian";

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
}
