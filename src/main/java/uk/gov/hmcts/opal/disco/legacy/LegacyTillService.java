package uk.gov.hmcts.opal.disco.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyTillSearchResults;
import uk.gov.hmcts.opal.dto.search.TillSearchDto;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.disco.TillServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "opal.LegacyTillService")
public class LegacyTillService extends LegacyService implements TillServiceInterface {

    public LegacyTillService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public TillEntity getTill(long tillId) {
        log.debug("getTill for {} from {}", tillId, legacyGateway.getUrl());
        return postToGateway("getTill", TillEntity.class, tillId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<TillEntity> searchTills(TillSearchDto criteria) {
        log.debug(":searchTills: criteria: {} via gateway {}", criteria.toJson(), legacyGateway.getUrl());
        return postToGateway("searchTills", LegacyTillSearchResults.class, criteria)
            .getTillEntities();
    }

}
