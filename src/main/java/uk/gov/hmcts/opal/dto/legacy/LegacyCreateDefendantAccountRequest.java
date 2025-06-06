package uk.gov.hmcts.opal.dto.legacy;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class LegacyCreateDefendantAccountRequest {

    @JsonProperty("business_unit_id")
    private Short businessUnitId;

    @JsonProperty("business_unit_user_id")
    private String businessUnitUserId;

    @JsonProperty("defendant_account")
    private String defendantAccount;
}
