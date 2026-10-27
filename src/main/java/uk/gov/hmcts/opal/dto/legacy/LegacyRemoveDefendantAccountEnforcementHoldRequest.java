package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigInteger;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LegacyRemoveDefendantAccountEnforcementHoldRequest {
    @JsonProperty("defendant_account_id")
    private String defendantAccountId;

    @JsonProperty("business_unit_id")
    private String businessUnitId;

    @JsonProperty("business_unit_user_id")
    private String businessUnitUserId;

    @JsonProperty("version")
    private BigInteger version;

    @JsonProperty("reason")
    private String reason;
}