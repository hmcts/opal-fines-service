package uk.gov.hmcts.opal.service.opal;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;
import uk.gov.hmcts.opal.util.LogUtil;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractDraftAccountPdplLoggingService {

    protected final LoggingService loggingService;
    protected final Clock clock;

    protected void logDraftAccountPersonalDataProcessing(
        DraftAccountEntity entity,
        String businessIdentifier
    ) {

        ParticipantIdentifier individuals = ParticipantIdentifier.builder()
            .identifier(entity.getDraftAccountId().toString())
            .type(PdplIdentifierType.DRAFT_ACCOUNT)
            .build();

        ParticipantIdentifier createdBy = ParticipantIdentifier.builder()
            .identifier(entity.getSubmittedBy())
            .type(PdplIdentifierType.OPAL_USER_ID)
            .build();

        PersonalDataProcessingLogDetails logDetails =
            PersonalDataProcessingLogDetails.builder()
                .recipient(null)
                .businessIdentifier(businessIdentifier)
                .category(PersonalDataProcessingCategory.COLLECTION)
                .ipAddress(LogUtil.getIpAddress())
                .createdAt(OffsetDateTime.now(clock))
                .createdBy(createdBy)
                .individuals(List.of(individuals))
                .build();

        loggingService.personalDataAccessLogAsync(logDetails);
    }
}
