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
public class LegacyGetDefendantAccountRequest {

    @JsonProperty("defendant_account_id")
    private String defendantAccountId;

}
