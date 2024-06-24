package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.search.PrisonSearchDto;
import uk.gov.hmcts.opal.entity.PrisonEntity;
import uk.gov.hmcts.opal.service.PrisonServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyPrisonService")
public class LegacyPrisonService extends LegacyService implements PrisonServiceInterface {

    public LegacyPrisonService(LegacyGatewayProperties legacyGateway, RestClient restClient) {
        super(legacyGateway, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public PrisonEntity getPrison(long prisonId) {
        log.info("getPrison for {} from {}", prisonId, legacyGateway.getUrl());
        return postToGateway("getPrison", PrisonEntity.class, prisonId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<PrisonEntity> searchPrisons(PrisonSearchDto criteria) {
        log.info(":searchPrisons: criteria: {} via gateway {}", criteria.toJson(), legacyGateway.getUrl());
        return postToGateway("searchPrisons", List.class, criteria);
    }

}
