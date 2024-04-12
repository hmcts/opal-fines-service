package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.OffenseSearchDto;
import uk.gov.hmcts.opal.entity.OffenseEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.OffenseServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyOffenseService;
import uk.gov.hmcts.opal.service.opal.OffenseService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("offenseServiceProxy")
public class OffenseServiceProxy implements OffenseServiceInterface, ProxyInterface {

    private final OffenseService opalOffenseService;
    private final LegacyOffenseService legacyOffenseService;
    private final DynamicConfigService dynamicConfigService;

    private OffenseServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyOffenseService : opalOffenseService;
    }

    @Override
    public OffenseEntity getOffense(short offenseId) {
        return getCurrentModeService().getOffense(offenseId);
    }

    @Override
    public List<OffenseEntity> searchOffenses(OffenseSearchDto criteria) {
        return getCurrentModeService().searchOffenses(criteria);
    }
}
