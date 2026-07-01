package uk.gov.hmcts.opal.service.opal.history.majorcreditor;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.history.AccountHistoryContext;
import uk.gov.hmcts.opal.dto.history.AccountHistoryItem;
import uk.gov.hmcts.opal.dto.history.AccountHistoryResult;
import uk.gov.hmcts.opal.dto.history.AccountHistoryType;
import uk.gov.hmcts.opal.dto.response.GetMajorCreditorHistoryResponse;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.service.opal.history.HistoryItemOrderingService;
import uk.gov.hmcts.opal.service.opal.history.core.AbstractAccountHistoryService;
import uk.gov.hmcts.opal.service.opal.history.majorcreditor.sources.MajorCreditorTransactionHistorySource;

@Service
@Slf4j(topic = "opal.MajorCreditorHistoryService")
public class MajorCreditorHistoryService extends AbstractAccountHistoryService {

    private static final String MAJOR_CREDITOR_ACCOUNT_NOT_FOUND = "Major creditor account not found: ";

    private final CreditorAccountRepository creditorAccountRepository;
    private final HistoryItemOrderingService historyItemOrderingService;

    public MajorCreditorHistoryService(
        CreditorAccountRepository creditorAccountRepository,
        HistoryItemOrderingService historyItemOrderingService,
        MajorCreditorTransactionHistorySource transactionSource
    ) {
        super(List.of(transactionSource));
        this.creditorAccountRepository = creditorAccountRepository;
        this.historyItemOrderingService = historyItemOrderingService;
    }

    @Transactional(readOnly = true)
    public GetMajorCreditorHistoryResponse getHistory(
        Long majorCreditorAccountId,
        LocalDate dateFrom,
        LocalDate dateTo,
        List<String> itemTypes
    ) {
        log.debug(":getHistory: Opal mode - ID: {}", majorCreditorAccountId);

        AccountHistoryResult historyResult = super.getHistory(
            majorCreditorAccountId,
            MajorCreditorHistoryModelAdapter.toCoreFilter(dateFrom, dateTo, itemTypes)
        );

        return GetMajorCreditorHistoryResponse.builder()
            .payload(MajorCreditorHistoryModelAdapter.toGeneratedResponse(historyResult))
            .version(historyResult.getVersion())
            .build();
    }

    @Override
    protected AccountHistoryContext buildContext(Long accountId) {
        return new AccountHistoryContext(AccountHistoryType.MAJOR_CREDITOR, accountId);
    }

    @Override
    protected AccountHistoryContext ensureAccountExists(AccountHistoryContext context) {
        CreditorAccountEntity creditorAccount = creditorAccountRepository.findById(context.getAccountId())
            .orElseThrow(() -> new EntityNotFoundException(
                MAJOR_CREDITOR_ACCOUNT_NOT_FOUND + context.getAccountId()
            ));

        if (!isMajorCreditorAccount(creditorAccount.getCreditorAccountType())) {
            throw new EntityNotFoundException(MAJOR_CREDITOR_ACCOUNT_NOT_FOUND + context.getAccountId());
        }

        return context.withVersion(creditorAccount.getVersion());
    }

    @Override
    protected Comparator<AccountHistoryItem> getComparator() {
        return historyItemOrderingService.newestFirstComparator();
    }

    private boolean isMajorCreditorAccount(CreditorAccountType creditorAccountType) {
        return creditorAccountType != null
            && (creditorAccountType.isMajorCreditor() || creditorAccountType.isCentralFund());
    }
}
