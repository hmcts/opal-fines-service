package uk.gov.hmcts.opal.dto.search;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.dto.legacy.ReferenceNumberDto;

import java.util.List;

@Data
@Builder
public class AccountSearchDto implements ToJsonString {
    /** Business Unit IDs (optional). */
    @JsonProperty("business_unit_ids")
    @NotEmpty
    private List<Short> businessUnitIds;

    /** Active accounts only (required). */
    @JsonProperty("active_accounts_only")
    @NotNull
    private Boolean activeAccountsOnly;

    /** Reference Number (optional, mutually exclusive with Defendant). */
    @JsonProperty("reference_number")
    private ReferenceNumberDto referenceNumberDto;

    /** Defendant (optional, mutually exclusive with Reference Number). */
    @JsonProperty("defendant")
    @NotNull
    private DefendantDto defendant;

    @JsonProperty("consolidation_search")
    private boolean consolidationSearch;
}
