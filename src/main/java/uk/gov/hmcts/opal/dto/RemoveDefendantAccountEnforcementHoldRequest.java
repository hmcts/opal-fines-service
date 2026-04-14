package uk.gov.hmcts.opal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemoveDefendantAccountEnforcementHoldRequest {
    private String reason;
}