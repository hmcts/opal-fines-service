package uk.gov.hmcts.opal.service.opal.history.core;

import java.math.BigInteger;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountHistoryResult {

    private BigInteger version;

    private List<AccountHistoryItem> historyItems;
}
