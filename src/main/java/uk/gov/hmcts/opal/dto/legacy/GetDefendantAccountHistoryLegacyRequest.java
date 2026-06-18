package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetDefendantAccountHistoryLegacyRequest {

    @JsonProperty("defendant_account_id")
    private String defendantAccountId;

    @JsonProperty("from_date")
    private LocalDate fromDate;

    @JsonProperty("to_date")
    private LocalDate toDate;

    @JsonProperty("item_types")
    private List<String> itemTypes;
}
