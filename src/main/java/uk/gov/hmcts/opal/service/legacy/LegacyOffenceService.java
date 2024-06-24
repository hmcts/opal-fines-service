package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.search.OffenceSearchDto;
import uk.gov.hmcts.opal.entity.OffenceEntity;
import uk.gov.hmcts.opal.service.OffenceServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyOffenceService")
public class LegacyOffenceService extends LegacyService implements OffenceServiceInterface {

    public LegacyOffenceService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public OffenceEntity getOffence(long offenceId) {
        log.info("getOffence for {} from {}", offenceId, legacyGateway.getUrl());
        return postToGateway("getOffence", OffenceEntity.class, offenceId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<OffenceEntity> searchOffences(OffenceSearchDto criteria) {
        log.info(":searchOffences: criteria: {} via gateway {}", criteria.toJson(), legacyGateway.getUrl());
        return postToGateway("searchOffences", List.class, criteria);
    }

}
