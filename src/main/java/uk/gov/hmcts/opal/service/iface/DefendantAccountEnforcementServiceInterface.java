package uk.gov.hmcts.opal.service.iface;

import tools.jackson.core.JacksonException;
import uk.gov.hmcts.opal.dto.AddDefendantAccountEnforcementRequest;
import uk.gov.hmcts.opal.dto.AddEnforcementResponse;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.generated.model.RemoveEnforcementHoldRequestDefendantAccount;
import uk.gov.hmcts.opal.generated.model.RemoveEnforcementHoldResponseDefendantAccount;

public interface DefendantAccountEnforcementServiceInterface {

    AddEnforcementResponse addEnforcement(Long defendantAccountId,
                                          Short businessUnitId,
                                          String businessUnitUserId,
                                          String ifMatch,
                                          AddDefendantAccountEnforcementRequest request) throws JacksonException;

    EnforcementStatus getEnforcementStatus(Long defendantAccountId);

    RemoveEnforcementHoldResponseDefendantAccount removeEnforcementHold(
        Long defendantAccountId,
        Short businessUnitId,
        String businessUnitUserId,
        String ifMatch,
        RemoveEnforcementHoldRequestDefendantAccount request);
}
