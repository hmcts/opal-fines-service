package uk.gov.hmcts.opal.service.opal.history.defendant;

import jakarta.persistence.EntityNotFoundException;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryFilter;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryResponse;
import uk.gov.hmcts.opal.service.opal.history.HistoryItemOrderingService;
import uk.gov.hmcts.opal.service.opal.history.core.AbstractAccountHistoryService;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryContext;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryItem;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryResult;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryType;
import uk.gov.hmcts.opal.service.opal.history.defendant.sources.AmendmentHistorySource;
import uk.gov.hmcts.opal.service.opal.history.defendant.sources.DefendantTransactionHistorySource;
import uk.gov.hmcts.opal.service.opal.history.defendant.sources.EnforcementHistorySource;
import uk.gov.hmcts.opal.service.opal.history.defendant.sources.NoteHistorySource;
import uk.gov.hmcts.opal.service.opal.history.defendant.sources.PaymentTermsHistorySource;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;

@Service
@Slf4j(topic = "opal.DefendantAccountHistoryService")
public class DefendantAccountHistoryService extends AbstractAccountHistoryService {

    private static final String DEFENDANT_ACCOUNT_NOT_FOUND = "Defendant Account not found with id: ";

    private final DefendantAccountRepositoryService defendantAccountRepositoryService;

    private final HistoryItemOrderingService historyItemOrderingService;

    public DefendantAccountHistoryService(DefendantAccountRepositoryService defendantAccountRepositoryService,
                                         HistoryItemOrderingService historyItemOrderingService,
                                         AmendmentHistorySource amendmentSource,
                                         EnforcementHistorySource enforcementSource,
                                         NoteHistorySource noteSource,
                                         PaymentTermsHistorySource paymentTermsSource,
                                         DefendantTransactionHistorySource transactionSource) {
        super(List.of(
            amendmentSource,
            enforcementSource,
            noteSource,
            paymentTermsSource,
            transactionSource
        ));
        this.defendantAccountRepositoryService = defendantAccountRepositoryService;
        this.historyItemOrderingService = historyItemOrderingService;
    }

    @Transactional(readOnly = true)
    public DefendantAccountHistoryResponse getHistory(Long defendantAccountId, DefendantAccountHistoryFilter filter) {
        log.debug(":getHistorySources: Opal mode - ID: {}", defendantAccountId);

        AccountHistoryResult historyResult = super.getHistory(
            defendantAccountId,
            DefendantAccountHistoryModelAdapter.toCoreFilter(filter)
        );

        return DefendantAccountHistoryResponse.builder()
            .version(historyResult.getVersion())
            .historyItems(historyResult.getHistoryItems().stream()
                .map(DefendantAccountHistoryModelAdapter::toDefendantItem)
                .toList())
            .build();
    }

    @Override
    protected AccountHistoryContext buildContext(Long accountId) {
        return new AccountHistoryContext(AccountHistoryType.DEFENDANT, accountId);
    }

    @Override
    protected AccountHistoryContext ensureAccountExists(AccountHistoryContext context) {
        return defendantAccountRepositoryService.findByDefendantAccountId(context.getAccountId())
            .map(defendantAccount -> context.withVersion(defendantAccount.getVersion()))
            .orElseThrow(() -> new EntityNotFoundException(DEFENDANT_ACCOUNT_NOT_FOUND + context.getAccountId()));
    }

    @Override
    protected Comparator<AccountHistoryItem> getComparator() {
        return historyItemOrderingService.newestFirstComparator();
    }
}
