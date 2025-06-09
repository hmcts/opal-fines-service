package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyConfigurationItemSearchResults;
import uk.gov.hmcts.opal.dto.search.ConfigurationItemSearchDto;
import uk.gov.hmcts.opal.entity.configurationitem.ConfigurationItemEntity;
import uk.gov.hmcts.opal.service.ConfigurationItemServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "opal.LegacyConfigurationItemService")
public class LegacyConfigurationItemService extends LegacyService implements ConfigurationItemServiceInterface {

    public LegacyConfigurationItemService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public ConfigurationItemEntity getConfigurationItem(long configurationItemId) {
        log.debug("getConfigurationItem for {} from {}", configurationItemId, legacyGateway.getUrl());
        return postToGateway("getConfigurationItem", ConfigurationItemEntity.class, configurationItemId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ConfigurationItemEntity> searchConfigurationItems(ConfigurationItemSearchDto criteria) {
        log.debug(":searchConfigurationItems: criteria: {} via gateway {}", criteria.toJson(), legacyGateway.getUrl());
        return postToGateway("searchConfigurationItems", LegacyConfigurationItemSearchResults.class, criteria)
            .getConfigurationItemEntities();
    }

}
