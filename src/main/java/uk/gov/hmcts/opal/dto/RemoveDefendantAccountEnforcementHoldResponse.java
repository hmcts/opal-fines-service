package uk.gov.hmcts.opal.dto;

import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemoveDefendantAccountEnforcementHoldResponse {
    private String defendantAccountId;
    private BigInteger version;
}