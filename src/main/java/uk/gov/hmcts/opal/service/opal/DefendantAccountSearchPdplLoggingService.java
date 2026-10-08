package uk.gov.hmcts.opal.service.opal;

import java.time.Clock;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.DefendantAccountSummaryDto;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.exception.PdplLoggingException;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

@Service
@Slf4j(topic = "opal.pdplLoggingService")
public class DefendantAccountSearchPdplLoggingService extends AbstractPdplLoggingService {

    private static final String BUSINESS_IDENTIFIER = "Search Account Results - Individual Defendants";
    private static final String QUEUE_FAILURE_MESSAGE = "Unable to queue PDPO log for defendant account search";

    public DefendantAccountSearchPdplLoggingService(LoggingService loggingService, Clock clock) {
        super(loggingService, clock);
    }

    public void logSearchResults(UserState userState, DefendantAccountSearchResultsDto results) {
        List<ParticipantIdentifier> individuals = getIndividualDefendants(results);

        if (individuals.isEmpty()) {
            return;
        }

        boolean queued;
        try {
            queued = logPdpl(BUSINESS_IDENTIFIER, PersonalDataProcessingCategory.CONSULTATION,
                individuals, null, userState.getUserId());
        } catch (RuntimeException ex) {
            log.error(QUEUE_FAILURE_MESSAGE, ex);
            throw new PdplLoggingException(QUEUE_FAILURE_MESSAGE, ex);
        }

        if (!queued) {
            log.error(QUEUE_FAILURE_MESSAGE);
            throw new PdplLoggingException(QUEUE_FAILURE_MESSAGE);
        }
    }

    private List<ParticipantIdentifier> getIndividualDefendants(DefendantAccountSearchResultsDto results) {
        if (results == null || results.getDefendantAccounts() == null) {
            return List.of();
        }

        return results.getDefendantAccounts().stream()
            .filter(result -> result != null && Boolean.FALSE.equals(result.getOrganisation()))
            .map(DefendantAccountSummaryDto::getDefendantAccountId)
            .distinct()
            .map(identifier -> ParticipantIdentifier.builder()
                .identifier(identifier)
                .type(PdplIdentifierType.DEFENDANT)
                .build())
            .toList();
    }
}
