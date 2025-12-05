package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.opal.dto.common.DefendantAccountParty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class LegacyReplaceDefendantAccountPartyRequest {

    @JsonProperty("version")
    private Long version;

    @JsonProperty("defendant_account_id")
    private Long defendantAccountId;

    @JsonProperty("business_unit_id")
    private String businessUnitId;

    @JsonProperty("business_unit_user_id")
    private String businessUnitUserId;

    @JsonProperty("defendant_account_party")
    private DefendantAccountParty defendantAccountParty;

}
