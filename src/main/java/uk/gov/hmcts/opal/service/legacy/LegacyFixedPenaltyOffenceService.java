package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.searchResults.LegacyFixedPenaltyOffenceSearchResults;
import uk.gov.hmcts.opal.dto.search.FixedPenaltyOffenceSearchDto;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity;
import uk.gov.hmcts.opal.service.FixedPenaltyOffenceServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyFixedPenaltyOffenceService")
public class LegacyFixedPenaltyOffenceService extends LegacyService implements FixedPenaltyOffenceServiceInterface {

    public LegacyFixedPenaltyOffenceService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public FixedPenaltyOffenceEntity getFixedPenaltyOffence(long fixedPenaltyOffenceId) {
        log.info("getFixedPenaltyOffence for {} from {}", fixedPenaltyOffenceId, legacyGateway.getUrl());
        return postToGateway("getFixedPenaltyOffence", FixedPenaltyOffenceEntity.class, fixedPenaltyOffenceId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<FixedPenaltyOffenceEntity> searchFixedPenaltyOffences(FixedPenaltyOffenceSearchDto criteria) {
        log.info(":searchFixedPenaltyOffences: criteria: {} via gateway {}", criteria.toJson(), legacyGateway.getUrl());
        return postToGateway("searchFixedPenaltyOffences", LegacyFixedPenaltyOffenceSearchResults.class, criteria)
            .getFixedPenaltyOffenceEntities();
    }

}
