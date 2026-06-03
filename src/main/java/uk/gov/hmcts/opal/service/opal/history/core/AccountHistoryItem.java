package uk.gov.hmcts.opal.service.opal.history.core;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.PostedDetails;
import uk.gov.hmcts.opal.dto.history.HistoryItemType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountHistoryItem {

    private PostedDetails postedDetails;

    private HistoryItemType type;

    private Object details;

    private BigDecimal amount;

    private LocalDateTime eventDateTime;

    private Long sourceId;
}
