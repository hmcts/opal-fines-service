package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MinorCreditorSearch implements ToJsonString {

    @JsonProperty("business_unit_ids")
    private List<Short> businessUnitIds;

    @JsonProperty("active_accounts_only")
    private Boolean activeAccountsOnly;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("creditor")
    private Creditor creditor;

}
