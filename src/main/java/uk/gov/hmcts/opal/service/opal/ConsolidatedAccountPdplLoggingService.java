package uk.gov.hmcts.opal.service.opal;

import static uk.gov.hmcts.opal.dto.PdplIdentifierType.DEFENDANT;
import static uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory.CONSULTATION;

import java.time.Clock;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.GetDefendantAccountConsolidatedAccountsResult;
import uk.gov.hmcts.opal.generated.model.ConsolidatedAccountDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountServiceProxy;

@Service
public class ConsolidatedAccountPdplLoggingService extends AbstractPdplLoggingService {

    private static final String BUSINESS_IDENTIFIER = "View Consolidated Accounts - Individual";
    private static final String QUEUE_FAILURE_MESSAGE = "Unable to queue PDPO log for consolidated accounts";

    private final DefendantAccountServiceProxy defendantAccountServiceProxy;

    public ConsolidatedAccountPdplLoggingService(LoggingService loggingService, Clock clock,
                                                  DefendantAccountServiceProxy defendantAccountServiceProxy) {
        super(loggingService, clock);
        this.defendantAccountServiceProxy = defendantAccountServiceProxy;
    }

    public void logIndividualChildAccounts(UserState userState, GetDefendantAccountConsolidatedAccountsResult result) {
        if (result.getPayload() == null || result.getPayload().isEmpty()) {
            return;
        }

        List<Long> individualChildAccountIds = result.getPayload().stream()
            .map(ConsolidatedAccountDefendantAccount::getAccountId)
            .distinct()
            .filter(this::isIndividualChildAccount)
            .toList();

        if (individualChildAccountIds.isEmpty()) {
            return;
        }

        List<ParticipantIdentifier> individuals = individualChildAccountIds.stream()
            .map(id -> participant(id.toString(), DEFENDANT))
            .toList();

        logPdplOrThrow(BUSINESS_IDENTIFIER, CONSULTATION, individuals, null, userState.getUserId(),
            QUEUE_FAILURE_MESSAGE);
    }

    private boolean isIndividualChildAccount(Long childAccountId) {
        if (childAccountId == null) {
            throw new IllegalStateException("Consolidated child account ID is missing");
        }

        DefendantAccountHeaderSummary header = defendantAccountServiceProxy.getHeaderSummary(childAccountId);
        if (header == null || header.getResponse() == null) {
            throw new IllegalStateException("Organisation flag is missing for child account " + childAccountId);
        }

        PartyDetailsCommon partyDetails = header.getResponse().getPartyDetails();
        if (partyDetails == null || partyDetails.getOrganisationFlag() == null) {
            throw new IllegalStateException("Organisation flag is missing for child account " + childAccountId);
        }
        // An organisation flag of false identifies an individual defendant
        return !partyDetails.getOrganisationFlag();
    }
}
