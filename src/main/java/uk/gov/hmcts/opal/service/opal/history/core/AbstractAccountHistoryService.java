package uk.gov.hmcts.opal.service.opal.history.core;

import java.util.Comparator;
import java.util.List;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryFilter;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryItem;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryResponse;

public abstract class AbstractAccountHistoryService {

    private final List<AccountHistorySource> sources;

    protected AbstractAccountHistoryService(List<AccountHistorySource> sources) {
        this.sources = sources;
    }

    public DefendantAccountHistoryResponse getHistory(Long accountId, DefendantAccountHistoryFilter filter) {
        AccountHistoryContext context = buildContext(accountId);

        ensureAccountExists(context);

        List<DefendantAccountHistoryItem> items = sources.stream()
            .filter(source -> source.supports(context))
            .filter(source -> filter.includes(source.getItemType()))
            .flatMap(source -> source.fetch(context, filter).stream())
            .sorted(getComparator())
            .toList();

        return buildResponse(context, items);
    }

    protected abstract AccountHistoryContext buildContext(Long accountId);

    protected abstract void ensureAccountExists(AccountHistoryContext context);

    protected abstract Comparator<DefendantAccountHistoryItem> getComparator();

    protected abstract DefendantAccountHistoryResponse buildResponse(AccountHistoryContext context,
                                                                    List<DefendantAccountHistoryItem> items);
}
