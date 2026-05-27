package uk.gov.hmcts.opal.dto.history;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.PostedDetails;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DefendantAccountHistoryItem implements ToJsonString {

    private PostedDetails postedDetails;

    private HistoryItemType type;

    private Object details;

    private BigDecimal amount;

    @JsonIgnore
    private LocalDateTime eventDateTime;

    @JsonIgnore
    private Long sourceId;
}
