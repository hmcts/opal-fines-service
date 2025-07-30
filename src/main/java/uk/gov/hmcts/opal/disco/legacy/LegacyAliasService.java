package uk.gov.hmcts.opal.disco.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyAliasSearchResults;
import uk.gov.hmcts.opal.dto.search.AliasSearchDto;
import uk.gov.hmcts.opal.entity.AliasEntity;
import uk.gov.hmcts.opal.disco.AliasServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "opal.LegacyAliasService")
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
        log.debug("getAlias for {} from {}", aliasId, legacyGateway.getUrl());
        return postToGateway("getAlias", AliasEntity.class, aliasId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<AliasEntity> searchAliass(AliasSearchDto criteria) {
        log.debug(":searchAliass: criteria: {} via gateway {}", criteria.toJson(), legacyGateway.getUrl());
        return postToGateway("searchAliass", LegacyAliasSearchResults.class, criteria)
            .getAliasEntities();
    }

}
