package uk.gov.hmcts.opal.dto.legacy;

import java.math.BigInteger;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LegacyRemoveDefendantAccountEnforcementHoldRequest {
    private String defendantAccountId;
    private String businessUnitId;
    private String businessUnitUserId;
    private BigInteger version;
    private String reason;
}