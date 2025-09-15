package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetDefendantAccountPartyLegacyRequest {

    @JsonProperty("defendant_account_id")
    private String defendantAccountId;

    @JsonProperty("defendant_account_party_id")
    private String defendantAccountPartyId;
}
