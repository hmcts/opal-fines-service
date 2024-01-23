package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class LegacyAccountDetailsRequestDto implements ToJsonString {

    //defendant_accounts.defendant_account_id
    @JsonProperty("defendant_account_id")
    private Long defendantAccountId;

}
