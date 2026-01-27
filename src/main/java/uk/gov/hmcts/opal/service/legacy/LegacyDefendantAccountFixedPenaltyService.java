package uk.gov.hmcts.opal.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.GetDefendantAccountFixedPenaltyResponse;
import uk.gov.hmcts.opal.service.iface.DefendantAccountFixedPenaltyServiceInterface;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.LegacyDefendantAccountFixedPenaltyService")
public class LegacyDefendantAccountFixedPenaltyService implements DefendantAccountFixedPenaltyServiceInterface {

    @Override
    public GetDefendantAccountFixedPenaltyResponse getDefendantAccountFixedPenalty(Long defendantAccountId) {
        throw new UnsupportedOperationException("Legacy GetDefendantAccountFixedPenalty not implemented yet");
    }
}
