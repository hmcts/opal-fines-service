package uk.gov.hmcts.opal.service.opal;

import static uk.gov.hmcts.opal.dto.PdplIdentifierType.DEFENDANT;
import static uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory.CONSULTATION;

import java.time.Clock;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.DefendantAccountSummaryDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

@Service
public class DefendantAccountSearchPdplLoggingService extends AbstractPdplLoggingService {

    private static final String BUSINESS_IDENTIFIER = "Search Account Results - Individual Defendants";
    private static final String CONSOLIDATION_BUSINESS_IDENTIFIER =
        "Search Account Results - Consolidation - Individual Defendants";
    private static final String QUEUE_FAILURE_MESSAGE = "Unable to queue PDPO log for defendant account search";

    public DefendantAccountSearchPdplLoggingService(LoggingService loggingService, Clock clock) {
        super(loggingService, clock);
    }

    public void logSearchResults(UserState userState, DefendantAccountSearchResultsDto results,
        boolean consolidationSearch) {

        List<ParticipantIdentifier> individuals = getIndividualDefendants(results);

        if (individuals.isEmpty()) {
            return;
        }

        String businessIdentifier = consolidationSearch ? CONSOLIDATION_BUSINESS_IDENTIFIER : BUSINESS_IDENTIFIER;
        logPdplOrThrow(businessIdentifier, CONSULTATION, individuals, null, userState.getUserId(),
            QUEUE_FAILURE_MESSAGE);
    }

    private List<ParticipantIdentifier> getIndividualDefendants(DefendantAccountSearchResultsDto results) {
        if (results == null || results.getDefendantAccounts() == null) {
            return List.of();
        }

        return results.getDefendantAccounts().stream()
            .filter(result -> result != null && Boolean.FALSE.equals(result.getOrganisation()))
            .map(DefendantAccountSummaryDto::getDefendantAccountId)
            .distinct()
            .map(identifier -> participant(identifier, DEFENDANT))
            .toList();
    }
}
