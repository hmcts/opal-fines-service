package uk.gov.hmcts.opal.disco.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.EnforcementSearchDto;
import uk.gov.hmcts.opal.entity.EnforcementEntity;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.disco.EnforcementServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyEnforcementService;
import uk.gov.hmcts.opal.disco.opal.EnforcementService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("enforcementServiceProxy")
public class EnforcementServiceProxy implements EnforcementServiceInterface, ProxyInterface {

    private final EnforcementService opalEnforcementService;
    private final LegacyEnforcementService legacyEnforcementService;
    private final DynamicConfigService dynamicConfigService;

    private EnforcementServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyEnforcementService : opalEnforcementService;
    }

    @Override
    public EnforcementEntity getEnforcement(long enforcementId) {
        return getCurrentModeService().getEnforcement(enforcementId);
    }

    @Override
    public List<EnforcementEntity> searchEnforcements(EnforcementSearchDto criteria) {
        return getCurrentModeService().searchEnforcements(criteria);
    }
}
