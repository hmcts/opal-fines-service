package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.AddPaymentCardRequestResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.service.iface.DefendantAccountPaymentTermsServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyDefendantAccountPaymentTermsService;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.OpalDefendantAccountPaymentTermsService;

@Service
@Slf4j(topic = "opal.DefendantAccountPaymentTermsServiceProxy")
@RequiredArgsConstructor
public class DefendantAccountPaymentTermsServiceProxy implements DefendantAccountPaymentTermsServiceInterface,
    ProxyInterface {

    private final OpalDefendantAccountPaymentTermsService draftAccountPromotion;
    private final LegacyDefendantAccountPaymentTermsService legacyDraftAccountPromotion;
    private final DynamicConfigService dynamicConfigService;

    private DefendantAccountPaymentTermsServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyDraftAccountPromotion : draftAccountPromotion;
    }

    @Override
    public GetDefendantAccountPaymentTermsResponse getPaymentTerms(Long defendantAccountId) {
        return getCurrentModeService().getPaymentTerms(defendantAccountId);
    }

    @Override
    public AddPaymentCardRequestResponse addPaymentCardRequest(Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String ifMatch,
        String authHeader) {
        return getCurrentModeService().addPaymentCardRequest(defendantAccountId, businessUnitId,
            businessUnitUserId, ifMatch, authHeader);
    }
}
