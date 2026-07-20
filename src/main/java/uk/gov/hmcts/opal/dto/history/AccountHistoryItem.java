package uk.gov.hmcts.opal.dto.history;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountHistoryItem {

    private AccountHistoryPostedDetails postedDetails;

    private AccountHistoryItemType type;

    private AccountHistoryDetails details;

    private BigDecimal amount;

    private LocalDateTime eventDateTime;

    private Long sourceId;
}
