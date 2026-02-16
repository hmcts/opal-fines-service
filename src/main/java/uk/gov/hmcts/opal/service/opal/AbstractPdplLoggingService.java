package uk.gov.hmcts.opal.service.opal;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;
import uk.gov.hmcts.opal.util.LogUtil;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractPdplLoggingService {

    protected final LoggingService loggingService;
    protected final Clock clock;

    protected void logPdpl(String businessIdentifier,
        PersonalDataProcessingCategory category,
        List<ParticipantIdentifier> individuals,
        ParticipantIdentifier recipient,
        UserState userState) {


        // attempt to resolve createdBy from Spring Security
        ParticipantIdentifier createdBy = ParticipantIdentifier.builder()
            .identifier(userState.getUserId().toString())
            .type(PdplIdentifierType.OPAL_USER_ID)
            .build();

        PersonalDataProcessingLogDetails logDetails = PersonalDataProcessingLogDetails.builder()
            .recipient(recipient)
            .businessIdentifier(businessIdentifier)
            .category(category)
            .ipAddress(LogUtil.getIpAddress())
            .createdAt(OffsetDateTime.now(clock))
            .createdBy(createdBy)
            .individuals(individuals)
            .build();

        loggingService.personalDataAccessLogAsync(logDetails);
    }
}
