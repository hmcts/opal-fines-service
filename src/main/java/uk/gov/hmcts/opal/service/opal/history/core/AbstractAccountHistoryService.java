package uk.gov.hmcts.opal.service.opal.history.core;

import java.util.Comparator;
import java.util.List;
import uk.gov.hmcts.opal.dto.history.AccountHistoryContext;
import uk.gov.hmcts.opal.dto.history.AccountHistoryFilter;
import uk.gov.hmcts.opal.dto.history.AccountHistoryItem;
import uk.gov.hmcts.opal.dto.history.AccountHistoryResult;

public abstract class AbstractAccountHistoryService {

    private final List<AccountHistorySource> sources;

    protected AbstractAccountHistoryService(List<AccountHistorySource> sources) {
        this.sources = sources;
    }

    protected AccountHistoryResult getHistory(Long accountId, AccountHistoryFilter filter) {
        AccountHistoryContext context = buildContext(accountId);

        AccountHistoryContext loadedContext = ensureAccountExists(context);

        List<AccountHistoryItem> items = sources.stream()
            .filter(source -> source.supports(loadedContext))
            .filter(source -> filter.includes(source.getItemType()))
            .flatMap(source -> source.fetch(loadedContext, filter).stream())
            .sorted(getComparator())
            .toList();

        return AccountHistoryResult.builder()
            .version(loadedContext.getVersion())
            .historyItems(items)
            .build();
    }

    protected abstract AccountHistoryContext buildContext(Long accountId);

    protected abstract AccountHistoryContext ensureAccountExists(AccountHistoryContext context);

    protected abstract Comparator<AccountHistoryItem> getComparator();
}
