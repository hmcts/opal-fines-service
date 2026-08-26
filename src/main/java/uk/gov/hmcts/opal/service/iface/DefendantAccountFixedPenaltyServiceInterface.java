package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.generated.model.GetDefendantAccountFixedPenaltyResponse;

public interface DefendantAccountFixedPenaltyServiceInterface {

    GetDefendantAccountFixedPenaltyResponse getDefendantAccountFixedPenalty(Long defendantAccountId);
}
