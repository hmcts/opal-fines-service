package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityNotFoundException;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryFilter;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryItem;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryResponse;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.service.opal.history.HistoryItemOrderingService;
import uk.gov.hmcts.opal.service.opal.history.core.AbstractAccountHistoryService;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryContext;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryType;
import uk.gov.hmcts.opal.service.opal.history.source.AmendmentHistorySourceService;
import uk.gov.hmcts.opal.service.opal.history.source.DefendantTransactionHistorySourceService;
import uk.gov.hmcts.opal.service.opal.history.source.EnforcementHistorySourceService;
import uk.gov.hmcts.opal.service.opal.history.source.NoteHistorySourceService;
import uk.gov.hmcts.opal.service.opal.history.source.PaymentTermsHistorySourceService;

@Service
@Slf4j(topic = "opal.OpalDefendantAccountHistoryService")
public class OpalDefendantAccountHistoryService extends AbstractAccountHistoryService {

    private static final String DEFENDANT_ACCOUNT_NOT_FOUND = "Defendant Account not found with id: ";

    private final DefendantAccountRepository defendantAccountRepository;

    private final HistoryItemOrderingService historyItemOrderingService;

    public OpalDefendantAccountHistoryService(DefendantAccountRepository defendantAccountRepository,
                                              HistoryItemOrderingService historyItemOrderingService,
                                              AmendmentHistorySourceService amendmentSource,
                                              EnforcementHistorySourceService enforcementSource,
                                              NoteHistorySourceService noteSource,
                                              PaymentTermsHistorySourceService paymentTermsSource,
                                              DefendantTransactionHistorySourceService transactionSource) {
        super(List.of(
            amendmentSource,
            enforcementSource,
            noteSource,
            paymentTermsSource,
            transactionSource
        ));
        this.defendantAccountRepository = defendantAccountRepository;
        this.historyItemOrderingService = historyItemOrderingService;
    }

    @Override
    @Transactional(readOnly = true)
    public DefendantAccountHistoryResponse getHistory(Long defendantAccountId, DefendantAccountHistoryFilter filter) {
        log.debug(":getHistorySources: Opal mode - ID: {}", defendantAccountId);
        return super.getHistory(defendantAccountId, filter);
    }

    @Override
    protected AccountHistoryContext buildContext(Long accountId) {
        return new AccountHistoryContext(AccountHistoryType.DEFENDANT, accountId);
    }

    @Override
    protected void ensureAccountExists(AccountHistoryContext context) {
        defendantAccountRepository.findByDefendantAccountId(context.getAccountId())
            .orElseThrow(() -> new EntityNotFoundException(DEFENDANT_ACCOUNT_NOT_FOUND + context.getAccountId()));
    }

    @Override
    protected Comparator<DefendantAccountHistoryItem> getComparator() {
        return historyItemOrderingService.newestFirstComparator();
    }

    @Override
    protected DefendantAccountHistoryResponse buildResponse(AccountHistoryContext context,
                                                           List<DefendantAccountHistoryItem> items) {
        return DefendantAccountHistoryResponse.builder()
            .version(defendantAccountRepository.findByDefendantAccountId(context.getAccountId())
                .orElseThrow(() -> new EntityNotFoundException(DEFENDANT_ACCOUNT_NOT_FOUND + context.getAccountId()))
                .getVersion())
            .historyItems(items)
            .build();
    }
}
