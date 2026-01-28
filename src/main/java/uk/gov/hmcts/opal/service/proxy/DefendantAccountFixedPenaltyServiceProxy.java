package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.GetDefendantAccountFixedPenaltyResponse;
import uk.gov.hmcts.opal.service.iface.DefendantAccountFixedPenaltyServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyDefendantAccountFixedPenaltyService;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.OpalDefendantAccountFixedPenaltyService;

@Service
@Slf4j(topic = "opal.DefendantAccountFixedPenaltyServiceProxy")
@RequiredArgsConstructor
public class DefendantAccountFixedPenaltyServiceProxy implements DefendantAccountFixedPenaltyServiceInterface,
    ProxyInterface {

    private final OpalDefendantAccountFixedPenaltyService draftAccountPromotion;
    private final LegacyDefendantAccountFixedPenaltyService legacyDraftAccountPromotion;
    private final DynamicConfigService dynamicConfigService;

    private DefendantAccountFixedPenaltyServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyDraftAccountPromotion : draftAccountPromotion;
    }

    @Override
    public GetDefendantAccountFixedPenaltyResponse getDefendantAccountFixedPenalty(Long defendantAccountId) {
        return getCurrentModeService().getDefendantAccountFixedPenalty(defendantAccountId);
    }
}
