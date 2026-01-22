package uk.gov.hmcts.opal.service.opal;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;
import uk.gov.hmcts.opal.util.LogUtil;

/**
 * Minimal shared PDPL logging base: only dependencies and a single log method.
 *
 * The method intentionally does not accept ipAddress or createdAt (they are derived here).
 * createdBy is attempted to be resolved from the Spring SecurityContext; if unavailable it will be null.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractPdplLoggingService {

    protected final LoggingService loggingService;
    protected final Clock clock;

    /**
     * Central PDPL logging helper. Subclasses provide businessIdentifier, category, and participants.
     *
     * @param businessIdentifier business-facing identifier / description for the log
     * @param category PDPL category (e.g. COLLECTION)
     * @param individuals list of individuals involved (participant identifiers)
     * @param recipient optional recipient
     */
    protected void logPdpl(String businessIdentifier,
        PersonalDataProcessingCategory category,
        List<ParticipantIdentifier> individuals,
        ParticipantIdentifier recipient) {

        // attempt to resolve createdBy from Spring Security
        ParticipantIdentifier createdBy = null;
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null) {
                createdBy = ParticipantIdentifier.builder()
                    .identifier(auth.getName())
                    .type(PdplIdentifierType.OPAL_USER_ID)
                    .build();
            }
        } catch (Exception ignored) {
            // if security is not available, createdBy remains null
        }

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
