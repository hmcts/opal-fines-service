package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantDto;

import java.util.List;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LegacyDefendantAccountSearchCriteria implements ToJsonString {
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


    public static LegacyDefendantAccountSearchCriteria fromAccountSearchDto(AccountSearchDto dto) {
        DefendantDto defendant = dto.getDefendant();
        return LegacyDefendantAccountSearchCriteria.builder()
                .businessUnitIds(dto.getBusinessUnitIds())
                .activeAccountsOnly(dto.getActiveAccountsOnly())
                .referenceNumberDto(dto.getReferenceNumberDto())
                .defendant(Objects.nonNull(defendant) ? defendant : new DefendantDto())
                .build();
    }

}
