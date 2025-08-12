package uk.gov.hmcts.opal.entity.minorcreditor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToJsonString;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MinorCreditorSearch implements ToJsonString {

    @JsonProperty("business_unit_ids")
    private List<Integer> businessUnitIds;

    @JsonProperty("active_accounts_only")
    private Boolean activeAccountsOnly;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("creditor")
    private Creditor creditor;

}
