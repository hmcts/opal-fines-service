package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.common.DefendantAccountParty;
import uk.gov.hmcts.opal.service.iface.DefendantAccountPartyServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyDefendantAccountPartyService;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.OpalDefendantAccountPartyService;

@Service
@Slf4j(topic = "opal.DefendantAccountPartyServiceProxy")
@RequiredArgsConstructor
public class DefendantAccountPartyServiceProxy implements DefendantAccountPartyServiceInterface, ProxyInterface {

    private final OpalDefendantAccountPartyService draftAccountPromotion;
    private final LegacyDefendantAccountPartyService legacyDraftAccountPromotion;
    private final DynamicConfigService dynamicConfigService;

    private DefendantAccountPartyServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyDraftAccountPromotion : draftAccountPromotion;
    }

    @Override
    public GetDefendantAccountPartyResponse getDefendantAccountParty(Long defendantAccountId,
                                                                     Long defendantAccountPartyId) {
        return getCurrentModeService().getDefendantAccountParty(defendantAccountId, defendantAccountPartyId);
    }

    @Override
    public GetDefendantAccountPartyResponse replaceDefendantAccountParty(Long defendantAccountId,
        Long defendantAccountPartyId,
        DefendantAccountParty defendantAccountParty, String ifMatch, String businessUnitId, String postedBy,
        String businessUserId) {

        return getCurrentModeService().replaceDefendantAccountParty(defendantAccountId, defendantAccountPartyId,
            defendantAccountParty, ifMatch, businessUnitId, postedBy, businessUserId);

    }
}
