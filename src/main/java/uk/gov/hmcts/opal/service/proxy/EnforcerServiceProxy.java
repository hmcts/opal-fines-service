package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.EnforcerSearchDto;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.service.EnforcerServiceInterface;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.legacy.LegacyEnforcerService;
import uk.gov.hmcts.opal.service.opal.EnforcerService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("enforcerServiceProxy")
public class EnforcerServiceProxy implements EnforcerServiceInterface, ProxyInterface {

    private final EnforcerService opalEnforcerService;
    private final LegacyEnforcerService legacyEnforcerService;
    private final DynamicConfigService dynamicConfigService;

    private EnforcerServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyEnforcerService : opalEnforcerService;
    }

    @Override
    public EnforcerEntity getEnforcer(long enforcerId) {
        return getCurrentModeService().getEnforcer(enforcerId);
    }

    @Override
    public List<EnforcerEntity> searchEnforcers(EnforcerSearchDto criteria) {
        return getCurrentModeService().searchEnforcers(criteria);
    }
}
