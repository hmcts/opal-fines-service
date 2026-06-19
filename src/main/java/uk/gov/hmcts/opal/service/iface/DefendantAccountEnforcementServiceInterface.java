package uk.gov.hmcts.opal.service.iface;

import tools.jackson.core.JacksonException;
import uk.gov.hmcts.opal.dto.AddDefendantAccountEnforcementRequest;
import uk.gov.hmcts.opal.dto.AddEnforcementResponse;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.dto.RemoveDefendantAccountEnforcementHoldRequest;
import uk.gov.hmcts.opal.dto.RemoveDefendantAccountEnforcementHoldResponse;

public interface DefendantAccountEnforcementServiceInterface {

    AddEnforcementResponse addEnforcement(Long defendantAccountId,
                                          Short businessUnitId,
                                          String businessUnitUserId,
                                          String ifMatch,
                                          AddDefendantAccountEnforcementRequest request) throws JacksonException;

    EnforcementStatus getEnforcementStatus(Long defendantAccountId);

    RemoveDefendantAccountEnforcementHoldResponse removeEnforcementHold(
        Long defendantAccountId,
        Short businessUnitId,
        String businessUnitUserId,
        String ifMatch,
        RemoveDefendantAccountEnforcementHoldRequest request);
}
