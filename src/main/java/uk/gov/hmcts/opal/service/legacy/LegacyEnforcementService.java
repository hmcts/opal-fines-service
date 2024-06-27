package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.searchResults.LegacyEnforcementSearchResults;
import uk.gov.hmcts.opal.dto.search.EnforcementSearchDto;
import uk.gov.hmcts.opal.entity.EnforcementEntity;
import uk.gov.hmcts.opal.service.EnforcementServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyEnforcementService")
public class LegacyEnforcementService extends LegacyService implements EnforcementServiceInterface {

    public LegacyEnforcementService(LegacyGatewayProperties legacyGateway, RestClient restClient) {
        super(legacyGateway, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public EnforcementEntity getEnforcement(long enforcementId) {
        log.info("getEnforcement for {} from {}", enforcementId, legacyGateway.getUrl());
        return postToGateway("getEnforcement", EnforcementEntity.class, enforcementId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<EnforcementEntity> searchEnforcements(EnforcementSearchDto criteria) {
        log.info(":searchEnforcements: criteria: {} via gateway {}", criteria.toJson(), legacyGateway.getUrl());
        return postToGateway("searchEnforcements", LegacyEnforcementSearchResults.class, criteria)
            .getEnforcementEntities();
    }

}
