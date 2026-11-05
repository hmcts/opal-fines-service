package uk.gov.hmcts.opal.service.opal;

import static uk.gov.hmcts.opal.dto.PdplIdentifierType.OPAL_USER_ID;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.opal.common.logging.LogUtil;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.exception.PdplLoggingException;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j(topic = "opal.pdplLoggingService")
public abstract class AbstractPdplLoggingService {

    protected final LoggingService loggingService;
    protected final Clock clock;

    protected boolean logPdpl(String businessIdentifier,
        PersonalDataProcessingCategory category,
        List<ParticipantIdentifier> individuals,
        ParticipantIdentifier recipient,
        UserState userState) {

        return logPdpl(businessIdentifier, category, individuals, recipient, userState.getUserId());
    }

    protected boolean logPdpl(String businessIdentifier,
        PersonalDataProcessingCategory category,
        List<ParticipantIdentifier> individuals,
        ParticipantIdentifier recipient,
        Long userId) {

        // attempt to resolve createdBy from Spring Security
        ParticipantIdentifier createdBy = participant(userId.toString(), OPAL_USER_ID);

        PersonalDataProcessingLogDetails logDetails = PersonalDataProcessingLogDetails.builder()
            .recipient(recipient)
            .businessIdentifier(businessIdentifier)
            .category(category)
            .ipAddress(LogUtil.getIpAddress())
            .createdAt(OffsetDateTime.now(clock))
            .createdBy(createdBy)
            .individuals(individuals)
            .build();

        return loggingService.personalDataAccessLogAsync(logDetails);
    }

    protected void logPdplOrThrow(String businessIdentifier,
        PersonalDataProcessingCategory category,
        List<ParticipantIdentifier> individuals,
        ParticipantIdentifier recipient,
        UserState userState,
        String failureMessage) {

        logPdplOrThrow(businessIdentifier, category, individuals, recipient, userState.getUserId(), failureMessage);
    }

    protected void logPdplOrThrow(String businessIdentifier,
        PersonalDataProcessingCategory category,
        List<ParticipantIdentifier> individuals,
        ParticipantIdentifier recipient,
        Long userId,
        String failureMessage) {

        boolean queued;
        try {
            queued = logPdpl(businessIdentifier, category, individuals, recipient, userId);
        } catch (RuntimeException ex) {
            log.error(failureMessage, ex);
            throw new PdplLoggingException(failureMessage, ex);
        }

        if (!queued) {
            log.error(failureMessage);
            throw new PdplLoggingException(failureMessage);
        }
    }

    protected ParticipantIdentifier participant(String identifier, PdplIdentifierType type) {
        return ParticipantIdentifier.builder()
            .identifier(identifier)
            .type(type)
            .build();
    }
}
