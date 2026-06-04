package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.common.DefendantAccountParty;
import uk.gov.hmcts.opal.dto.request.AddDefendantAccountPartyRequest;
import uk.gov.hmcts.opal.dto.request.RemoveDefendantAccountPartyRequest;
import uk.gov.hmcts.opal.dto.response.RemoveDefendantAccountPartyResponse;
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
    public GetDefendantAccountPartyResponse addDefendantAccountParty(Long defendantAccountId,
                                                                         String businessUnitId,
                                                                         String businessUserId,
                                                                         String postedBy,
                                                                         String postedByName,
                                                                         String ifMatch,
                                                                     AddDefendantAccountPartyRequest request) {

        return getCurrentModeService().addDefendantAccountParty(defendantAccountId,
                                                                businessUnitId,
                                                                businessUserId,
                                                                postedBy,
                                                                postedByName,
                                                                ifMatch,
                                                                request);
    }

    @Override
    public GetDefendantAccountPartyResponse replaceDefendantAccountParty(Long defendantAccountId,
        Long defendantAccountPartyId,
        DefendantAccountParty defendantAccountParty, String ifMatch, String businessUnitId, String postedBy,
        String postedByName, String businessUserId) {

        return getCurrentModeService().replaceDefendantAccountParty(defendantAccountId, defendantAccountPartyId,
            defendantAccountParty, ifMatch, businessUnitId, postedBy, postedByName, businessUserId);

    }

    @Override
    public RemoveDefendantAccountPartyResponse removeDefendantAccountParty(Long defendantAccountId,
        Long defendantAccountPartyId, Short businessUnitId, String businessUserId,
        String postedBy, String postedByName, String ifMatch, RemoveDefendantAccountPartyRequest request) {

        return getCurrentModeService().removeDefendantAccountParty(defendantAccountId,
            defendantAccountPartyId, businessUnitId, businessUserId,
            postedBy, postedByName, ifMatch, request);
    }
}
