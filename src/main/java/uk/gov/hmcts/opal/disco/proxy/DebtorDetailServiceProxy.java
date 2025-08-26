package uk.gov.hmcts.opal.disco.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.DebtorDetailSearchDto;
import uk.gov.hmcts.opal.entity.DebtorDetailEntity;
import uk.gov.hmcts.opal.disco.DebtorDetailServiceInterface;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.disco.legacy.LegacyDebtorDetailService;
import uk.gov.hmcts.opal.disco.opal.DebtorDetailService;
import uk.gov.hmcts.opal.service.proxy.ProxyInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("debtorDetailServiceProxy")
public class DebtorDetailServiceProxy implements DebtorDetailServiceInterface, ProxyInterface {

    private final DebtorDetailService opalDebtorDetailService;
    private final LegacyDebtorDetailService legacyDebtorDetailService;
    private final DynamicConfigService dynamicConfigService;

    private DebtorDetailServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyDebtorDetailService : opalDebtorDetailService;
    }

    @Override
    public DebtorDetailEntity getDebtorDetail(long debtorDetailId) {
        return getCurrentModeService().getDebtorDetail(debtorDetailId);
    }

    @Override
    public List<DebtorDetailEntity> searchDebtorDetails(DebtorDetailSearchDto criteria) {
        return getCurrentModeService().searchDebtorDetails(criteria);
    }
}
