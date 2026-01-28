package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.dto.GetDefendantAccountFixedPenaltyResponse;

public interface DefendantAccountFixedPenaltyServiceInterface {

    GetDefendantAccountFixedPenaltyResponse getDefendantAccountFixedPenalty(Long defendantAccountId);
}
