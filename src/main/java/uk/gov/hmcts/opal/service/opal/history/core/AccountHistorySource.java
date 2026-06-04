package uk.gov.hmcts.opal.service.opal.history.core;

import java.util.List;

public interface AccountHistorySource {

    boolean supports(AccountHistoryContext context);

    AccountHistoryItemType getItemType();

    List<AccountHistoryItem> fetch(AccountHistoryContext context, AccountHistoryFilter filter);
}
