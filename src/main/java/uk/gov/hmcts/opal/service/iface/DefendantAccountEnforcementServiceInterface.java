package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.dto.AddDefendantAccountEnforcementRequest;
import uk.gov.hmcts.opal.dto.AddEnforcementResponse;
import uk.gov.hmcts.opal.dto.EnforcementStatus;

public interface DefendantAccountEnforcementServiceInterface {

    AddEnforcementResponse addEnforcement(Long defendantAccountId, String businessUnitId, String businessUnitUserId,
        String ifMatch, String authHeader, AddDefendantAccountEnforcementRequest request);

    EnforcementStatus getEnforcementStatus(Long defendantAccountId);

}
