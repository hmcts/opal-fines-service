package uk.gov.hmcts.opal.service.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.AddDefendantAccountEnforcementRequest;
import uk.gov.hmcts.opal.dto.AddEnforcementResponse;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.service.iface.DefendantAccountEnforcementServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyDefendantAccountEnforcementService;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.OpalDefendantAccountEnforcementService;
import uk.gov.hmcts.opal.dto.RemoveDefendantAccountEnforcementHoldRequest;
import uk.gov.hmcts.opal.dto.RemoveDefendantAccountEnforcementHoldResponse;

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
    public AddEnforcementResponse addEnforcement(Long defendantAccountId,
                                                 Short businessUnitId,
                                                 String businessUnitUserId,
                                                 Long ifMatch,
                                                 String authHeader,
                                                 AddDefendantAccountEnforcementRequest request)
        throws JsonProcessingException {
        return getCurrentModeService().addEnforcement(defendantAccountId, businessUnitId, businessUnitUserId,
            ifMatch, authHeader, request);
    }

    @Override
    public RemoveDefendantAccountEnforcementHoldResponse removeEnforcementHold(
        Long defendantAccountId,
        Short businessUnitId,
        String businessUnitUserId,
        String ifMatch,
        String authHeader,
        RemoveDefendantAccountEnforcementHoldRequest request) {

        return getCurrentModeService().removeEnforcementHold(
            defendantAccountId,
            businessUnitId,
            businessUnitUserId,
            ifMatch,
            authHeader,
            request
        );
    }
}
