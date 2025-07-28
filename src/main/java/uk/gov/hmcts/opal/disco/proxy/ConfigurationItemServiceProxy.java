package uk.gov.hmcts.opal.disco.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.ConfigurationItemSearchDto;
import uk.gov.hmcts.opal.entity.configurationitem.ConfigurationItemEntity;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.disco.ConfigurationItemServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyConfigurationItemService;
import uk.gov.hmcts.opal.disco.opal.ConfigurationItemService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("configurationItemServiceProxy")
public class ConfigurationItemServiceProxy implements ConfigurationItemServiceInterface, ProxyInterface {

    private final ConfigurationItemService opalConfigurationItemService;
    private final LegacyConfigurationItemService legacyConfigurationItemService;
    private final DynamicConfigService dynamicConfigService;

    private ConfigurationItemServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyConfigurationItemService : opalConfigurationItemService;
    }

    @Override
    public ConfigurationItemEntity getConfigurationItem(long configurationItemId) {
        return getCurrentModeService().getConfigurationItem(configurationItemId);
    }

    @Override
    public List<ConfigurationItemEntity> searchConfigurationItems(ConfigurationItemSearchDto criteria) {
        return getCurrentModeService().searchConfigurationItems(criteria);
    }
}
