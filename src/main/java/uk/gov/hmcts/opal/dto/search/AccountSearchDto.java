package uk.gov.hmcts.opal.dto.search;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.dto.legacy.ReferenceNumberDto;

import java.util.List;
import java.util.Optional;

@Data
@Builder
public class AccountSearchDto implements ToJsonString {
    /** The court (CT) or MET (metropolitan area). */
    private String court;
    /** Defendant Surname, Company Name or A/C number. */
    @JsonProperty("search_type")
    private String searchType;
    private String pcr;
    /** Major Creditor account selection. */
    @JsonProperty("major_creditor")
    private String majorCreditor;
    /** Unsure. */
    @JsonProperty("till_number")
    private String tillNumber;

    /** Business Unit IDs (optional). */
    @JsonProperty("business_unit_ids")
    private List<Integer> businessUnitIds; // Matches "business_unit_ids" in the JSON schema

    /** Active accounts only (required). */
    @JsonProperty("active_accounts_only")
    private Boolean activeAccountsOnly; // Matches "active_accounts_only" in the JSON schema

    /** Reference Number (optional, mutually exclusive with Defendant). */
    @JsonProperty("reference_number")
    private ReferenceNumberDto referenceNumber; // Matches "reference_number" in the JSON schema

    /** Defendant (optional, mutually exclusive with Reference Number). */
    @JsonProperty("defendant")
    private DefendantDto defendant;

    @JsonIgnore
    public Optional<Long> getNumericCourt() {
        return Optional.ofNullable(getCourt())
            .filter(s -> s.matches("[0-9]+"))
            .map(Long::parseLong);
    }
}
