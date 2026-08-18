package uk.gov.hmcts.opal.service.opal.history.core;

import java.util.List;
import uk.gov.hmcts.opal.dto.history.AccountHistoryContext;
import uk.gov.hmcts.opal.dto.history.AccountHistoryFilter;
import uk.gov.hmcts.opal.dto.history.AccountHistoryItem;
import uk.gov.hmcts.opal.dto.history.AccountHistoryItemType;

public interface AccountHistorySource {

    boolean supports(AccountHistoryContext context);

    AccountHistoryItemType getItemType();

    List<AccountHistoryItem> fetch(AccountHistoryContext context, AccountHistoryFilter filter);
}
