package uk.gov.hmcts.opal.service.opal.history.core;

import java.util.List;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryFilter;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryItem;
import uk.gov.hmcts.opal.dto.history.HistoryItemType;

public interface AccountHistorySource {

    boolean supports(AccountHistoryContext context);

    HistoryItemType getItemType();

    List<DefendantAccountHistoryItem> fetch(AccountHistoryContext context, DefendantAccountHistoryFilter filter);
}
