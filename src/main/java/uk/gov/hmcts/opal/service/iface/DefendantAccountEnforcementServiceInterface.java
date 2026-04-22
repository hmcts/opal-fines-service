package uk.gov.hmcts.opal.service.iface;

import com.fasterxml.jackson.core.JsonProcessingException;
import uk.gov.hmcts.opal.dto.AddDefendantAccountEnforcementRequest;
import uk.gov.hmcts.opal.dto.AddEnforcementResponse;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.dto.RemoveDefendantAccountEnforcementHoldRequest;
import uk.gov.hmcts.opal.dto.RemoveDefendantAccountEnforcementHoldResponse;

public interface DefendantAccountEnforcementServiceInterface {

    AddEnforcementResponse addEnforcement(Long defendantAccountId,
                                          Short businessUnitId,
                                          String businessUnitUserId,
                                          Long ifMatch,
                                          String authHeader,
                                          AddDefendantAccountEnforcementRequest request) throws JsonProcessingException;

    EnforcementStatus getEnforcementStatus(Long defendantAccountId);

    RemoveDefendantAccountEnforcementHoldResponse removeEnforcementHold(
        Long defendantAccountId,
        Short businessUnitId,
        String businessUnitUserId,
        String ifMatch,
        String authHeader,
        RemoveDefendantAccountEnforcementHoldRequest request);
}
