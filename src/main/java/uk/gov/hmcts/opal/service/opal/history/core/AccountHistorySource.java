package uk.gov.hmcts.opal.service.opal.history.core;

import java.util.List;
import uk.gov.hmcts.opal.dto.history.HistoryItemType;

public interface AccountHistorySource {

    boolean supports(AccountHistoryContext context);

    HistoryItemType getItemType();

    List<AccountHistoryItem> fetch(AccountHistoryContext context, AccountHistoryFilter filter);
}
