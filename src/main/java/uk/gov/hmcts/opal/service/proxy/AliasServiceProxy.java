package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.AliasSearchDto;
import uk.gov.hmcts.opal.entity.AliasEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.AliasServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyAliasService;
import uk.gov.hmcts.opal.service.opal.AliasService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("aliasServiceProxy")
public class AliasServiceProxy implements AliasServiceInterface, ProxyInterface {

    private final AliasService opalAliasService;
    private final LegacyAliasService legacyAliasService;
    private final DynamicConfigService dynamicConfigService;

    private AliasServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyAliasService : opalAliasService;
    }

    @Override
    public AliasEntity getAlias(long aliasId) {
        return getCurrentModeService().getAlias(aliasId);
    }

    @Override
    public List<AliasEntity> searchAliass(AliasSearchDto criteria) {
        return getCurrentModeService().searchAliass(criteria);
    }
}
