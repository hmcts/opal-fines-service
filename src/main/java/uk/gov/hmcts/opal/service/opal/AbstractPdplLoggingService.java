package uk.gov.hmcts.opal.service.opal;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;
import uk.gov.hmcts.opal.util.LogUtil;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j(topic = "opal.pdplLoggingService")
public abstract class AbstractPdplLoggingService {

    protected final LoggingService loggingService;
    protected final Clock clock;

    protected void logPdpl(String businessIdentifier,
        PersonalDataProcessingCategory category,
        List<ParticipantIdentifier> individuals,
        ParticipantIdentifier recipient,
        DraftAccountEntity entity) {

        logPdpl(businessIdentifier, category, individuals, recipient, entity.getSubmittedBy(), entity);
    }

    protected void logPdpl(String businessIdentifier,
        PersonalDataProcessingCategory category,
        List<ParticipantIdentifier> individuals,
        ParticipantIdentifier recipient,
        String createdByIdentifier,
        DraftAccountEntity entity) {

        ParticipantIdentifier createdBy = ParticipantIdentifier.builder()
            .identifier(createdByIdentifier)
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

        try {
            boolean sent = loggingService.personalDataAccessLogAsync(logDetails);
            if (!sent) {
                log.error("PDPL log failed for businessIdentifier={} draftAccountId={}",
                          businessIdentifier, entity.getDraftAccountId());
            }
        } catch (RuntimeException ex) {
            log.error("PDPL log error for businessIdentifier={} draftAccountId={}",
                      businessIdentifier, entity.getDraftAccountId(), ex);
        }
    }
}
