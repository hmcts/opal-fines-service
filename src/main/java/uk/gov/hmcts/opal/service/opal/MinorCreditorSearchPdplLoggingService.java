package uk.gov.hmcts.opal.service.opal;

import static org.springframework.util.StringUtils.hasText;
import static uk.gov.hmcts.opal.dto.PdplIdentifierType.DEFENDANT;
import static uk.gov.hmcts.opal.dto.PdplIdentifierType.MINOR_CREDITOR;
import static uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory.CONSULTATION;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.exception.PdplLoggingException;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountSearchDefendant;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountSearchResultMinorCreditor;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

@Service
public class MinorCreditorSearchPdplLoggingService extends AbstractPdplLoggingService {

    private static final String CREDITOR_BUSINESS_IDENTIFIER = "Search Account Results - Individual Minor Creditors";
    private static final String DEFENDANT_BUSINESS_IDENTIFIER =
        "Search Account Results - Individual Defendants associated with Minor Creditors";
    private static final String QUEUE_FAILURE_MESSAGE = "Unable to queue PDPO log for minor creditor search";

    public MinorCreditorSearchPdplLoggingService(LoggingService loggingService, Clock clock) {
        super(loggingService, clock);
    }

    public void logSearchResults(UserState userState, MinorCreditorAccountsSearchResponse response) {
        if (response == null || response.getCreditorAccounts() == null) {
            return;
        }

        List<MinorCreditorAccountSearchResultMinorCreditor> accounts = response.getCreditorAccounts();
        List<ParticipantIdentifier> creditors = accounts.stream()
            .filter(Objects::nonNull)
            .filter(account -> isIndividual(account.getOrganisation()))
            .map(MinorCreditorAccountSearchResultMinorCreditor::getCreditorAccountId)
            .map(this::requiredIdentifier)
            .distinct()
            .map(id -> participant(id, MINOR_CREDITOR))
            .toList();
        List<ParticipantIdentifier> defendants = accounts.stream()
            .filter(Objects::nonNull)
            .map(MinorCreditorAccountSearchResultMinorCreditor::getDefendant)
            .filter(Objects::nonNull)
            .filter(this::hasDefendant)
            .filter(defendant -> isIndividual(defendant.getOrganisation()))
            .map(MinorCreditorAccountSearchDefendant::getDefendantAccountId)
            .map(this::requiredIdentifier)
            .distinct()
            .map(id -> participant(id, DEFENDANT))
            .toList();

        if (!creditors.isEmpty()) {
            logPdplOrThrow(CREDITOR_BUSINESS_IDENTIFIER, CONSULTATION, creditors, null, userState.getUserId(),
                QUEUE_FAILURE_MESSAGE);
        }
        if (!defendants.isEmpty()) {
            logPdplOrThrow(DEFENDANT_BUSINESS_IDENTIFIER, CONSULTATION, defendants, null, userState.getUserId(),
                QUEUE_FAILURE_MESSAGE);
        }
    }

    private boolean hasDefendant(MinorCreditorAccountSearchDefendant defendant) {
        return hasText(defendant.getDefendantAccountId())
            || hasText(defendant.getFirstnames())
            || hasText(defendant.getSurname())
            || hasText(defendant.getOrganisationName());
    }

    private boolean isIndividual(Boolean organisation) {
        if (organisation == null) {
            throw new PdplLoggingException("Unable to determine PDPO participants for minor creditor search");
        }
        return !organisation;
    }

    private String requiredIdentifier(String identifier) {
        if (!hasText(identifier)) {
            throw new PdplLoggingException("Unable to identify PDPO participant for minor creditor search");
        }
        return identifier;
    }
}
