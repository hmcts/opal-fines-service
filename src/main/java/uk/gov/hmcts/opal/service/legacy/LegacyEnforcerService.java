package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyEnforcerSearchResults;
import uk.gov.hmcts.opal.dto.search.EnforcerSearchDto;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.service.EnforcerServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyEnforcerService")
public class LegacyEnforcerService extends LegacyService implements EnforcerServiceInterface {

    public LegacyEnforcerService(LegacyGatewayProperties legacyGateway, RestClient restClient) {
        super(legacyGateway, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public EnforcerEntity getEnforcer(long enforcerId) {
        log.info("getEnforcer for {} from {}", enforcerId, legacyGateway.getUrl());
        return postToGateway("getEnforcer", EnforcerEntity.class, enforcerId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<EnforcerEntity> searchEnforcers(EnforcerSearchDto criteria) {
        log.info(":searchEnforcers: criteria: {} via gateway {}", criteria.toJson(), legacyGateway.getUrl());
        return postToGateway("searchEnforcers", LegacyEnforcerSearchResults.class, criteria)
            .getEnforcerEntities();
    }

}
