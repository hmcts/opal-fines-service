package uk.gov.hmcts.opal.service.proxy;

import tools.jackson.core.JacksonException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.generated.model.AddEnforcementRequestDefendantAccount;
import uk.gov.hmcts.opal.generated.model.AddEnforcementResponseDefendantAccount;
import uk.gov.hmcts.opal.generated.model.RemoveEnforcementHoldRequestDefendantAccount;
import uk.gov.hmcts.opal.generated.model.RemoveEnforcementHoldResponseDefendantAccount;
import uk.gov.hmcts.opal.service.iface.DefendantAccountEnforcementServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyDefendantAccountEnforcementService;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.OpalDefendantAccountEnforcementService;

@Service
@Slf4j(topic = "opal.DefendantAccountEnforcementServiceProxy")
@RequiredArgsConstructor
public class DefendantAccountEnforcementServiceProxy implements DefendantAccountEnforcementServiceInterface,
    ProxyInterface {

    private final OpalDefendantAccountEnforcementService draftAccountPromotion;
    private final LegacyDefendantAccountEnforcementService legacyDraftAccountPromotion;
    private final DynamicConfigService dynamicConfigService;

    private DefendantAccountEnforcementServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyDraftAccountPromotion : draftAccountPromotion;
    }

    @Override
    public EnforcementStatus getEnforcementStatus(Long defendantAccountId) {
        return getCurrentModeService().getEnforcementStatus(defendantAccountId);
    }

    @Override
    public AddEnforcementResponseDefendantAccount addEnforcement(Long defendantAccountId,
                                                 Short businessUnitId,
                                                 String businessUnitUserId,
                                                 String ifMatch,
                                                 AddEnforcementRequestDefendantAccount request)
        throws JacksonException {
        return getCurrentModeService().addEnforcement(defendantAccountId, businessUnitId, businessUnitUserId,
            ifMatch, request);
    }

    @Override
    public RemoveEnforcementHoldResponseDefendantAccount removeEnforcementHold(
        Long defendantAccountId,
        Short businessUnitId,
        String businessUnitUserId,
        String ifMatch,
        RemoveEnforcementHoldRequestDefendantAccount request) {

        return getCurrentModeService().removeEnforcementHold(
            defendantAccountId,
            businessUnitId,
            businessUnitUserId,
            ifMatch,
            request
        );
    }
}
