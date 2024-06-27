package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.searchResults.LegacyAliasSearchResults;
import uk.gov.hmcts.opal.dto.search.AliasSearchDto;
import uk.gov.hmcts.opal.entity.AliasEntity;
import uk.gov.hmcts.opal.service.AliasServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyAliasService")
public class LegacyAliasService extends LegacyService implements AliasServiceInterface {

    public LegacyAliasService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public AliasEntity getAlias(long aliasId) {
        log.info("getAlias for {} from {}", aliasId, legacyGateway.getUrl());
        return postToGateway("getAlias", AliasEntity.class, aliasId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<AliasEntity> searchAliass(AliasSearchDto criteria) {
        log.info(":searchAliass: criteria: {} via gateway {}", criteria.toJson(), legacyGateway.getUrl());
        return postToGateway("searchAliass", LegacyAliasSearchResults.class, criteria)
            .getAliasEntities();
    }

}
