package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.BacsPaymentSearchDto;
import uk.gov.hmcts.opal.entity.BacsPaymentEntity;
import uk.gov.hmcts.opal.service.BacsPaymentServiceInterface;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.legacy.LegacyBacsPaymentService;
import uk.gov.hmcts.opal.service.opal.BacsPaymentService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("bacsPaymentServiceProxy")
public class BacsPaymentServiceProxy implements BacsPaymentServiceInterface, ProxyInterface {

    private final BacsPaymentService opalBacsPaymentService;
    private final LegacyBacsPaymentService legacyBacsPaymentService;
    private final DynamicConfigService dynamicConfigService;

    private BacsPaymentServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyBacsPaymentService : opalBacsPaymentService;
    }

    @Override
    public BacsPaymentEntity getBacsPayment(long bacsPaymentId) {
        return getCurrentModeService().getBacsPayment(bacsPaymentId);
    }

    @Override
    public List<BacsPaymentEntity> searchBacsPayments(BacsPaymentSearchDto criteria) {
        return getCurrentModeService().searchBacsPayments(criteria);
    }
}
