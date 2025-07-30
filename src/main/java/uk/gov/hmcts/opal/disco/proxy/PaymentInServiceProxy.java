package uk.gov.hmcts.opal.disco.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.PaymentInSearchDto;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.disco.PaymentInServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyPaymentInService;
import uk.gov.hmcts.opal.disco.opal.PaymentInService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("paymentInServiceProxy")
public class PaymentInServiceProxy implements PaymentInServiceInterface, ProxyInterface {

    private final PaymentInService opalPaymentInService;
    private final LegacyPaymentInService legacyPaymentInService;
    private final DynamicConfigService dynamicConfigService;

    private PaymentInServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyPaymentInService : opalPaymentInService;
    }

    @Override
    public PaymentInEntity getPaymentIn(long paymentInId) {
        return getCurrentModeService().getPaymentIn(paymentInId);
    }

    @Override
    public List<PaymentInEntity> searchPaymentIns(PaymentInSearchDto criteria) {
        return getCurrentModeService().searchPaymentIns(criteria);
    }
}
