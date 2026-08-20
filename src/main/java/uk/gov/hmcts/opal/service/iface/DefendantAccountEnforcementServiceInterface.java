package uk.gov.hmcts.opal.service.iface;

import tools.jackson.core.JacksonException;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.dto.RemoveDefendantAccountEnforcementHoldRequest;
import uk.gov.hmcts.opal.dto.RemoveDefendantAccountEnforcementHoldResponse;
import uk.gov.hmcts.opal.generated.model.AddEnforcementRequestDefendantAccount;
import uk.gov.hmcts.opal.generated.model.AddEnforcementResponseDefendantAccount;

public interface DefendantAccountEnforcementServiceInterface {

    AddEnforcementResponseDefendantAccount addEnforcement(Long defendantAccountId,
                                                           Short businessUnitId,
                                                           String businessUnitUserId,
                                                           String ifMatch,
                                                           AddEnforcementRequestDefendantAccount request)
        throws JacksonException;

    EnforcementStatus getEnforcementStatus(Long defendantAccountId);

    RemoveDefendantAccountEnforcementHoldResponse removeEnforcementHold(
        Long defendantAccountId,
        Short businessUnitId,
        String businessUnitUserId,
        String ifMatch,
        RemoveDefendantAccountEnforcementHoldRequest request);
}
