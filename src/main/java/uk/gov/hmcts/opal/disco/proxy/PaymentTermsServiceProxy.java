package uk.gov.hmcts.opal.disco.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.PaymentTermsSearchDto;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.disco.PaymentTermsServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyPaymentTermsService;
import uk.gov.hmcts.opal.disco.opal.PaymentTermsService;
import uk.gov.hmcts.opal.service.proxy.ProxyInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("paymentTermsServiceProxy")
public class PaymentTermsServiceProxy implements PaymentTermsServiceInterface, ProxyInterface {

    private final PaymentTermsService opalPaymentTermsService;
    private final LegacyPaymentTermsService legacyPaymentTermsService;
    private final DynamicConfigService dynamicConfigService;

    private PaymentTermsServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyPaymentTermsService : opalPaymentTermsService;
    }

    @Override
    public PaymentTermsEntity getPaymentTerms(long paymentTermsId) {
        return getCurrentModeService().getPaymentTerms(paymentTermsId);
    }

    @Override
    public List<PaymentTermsEntity> searchPaymentTerms(PaymentTermsSearchDto criteria) {
        return getCurrentModeService().searchPaymentTerms(criteria);
    }
}
