package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.searchResults.LegacyLocalJusticeAreaSearchResults;
import uk.gov.hmcts.opal.dto.search.LocalJusticeAreaSearchDto;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.service.LocalJusticeAreaServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyLocalJusticeAreaService")
public class LegacyLocalJusticeAreaService extends LegacyService implements LocalJusticeAreaServiceInterface {


    public LegacyLocalJusticeAreaService(LegacyGatewayProperties legacyGateway, RestClient restClient) {
        super(legacyGateway, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public LocalJusticeAreaEntity getLocalJusticeArea(short localJusticeAreaId) {
        log.info("getLocalJusticeArea for {} from {}", localJusticeAreaId, legacyGateway.getUrl());
        return postToGateway("getLocalJusticeArea", LocalJusticeAreaEntity.class, localJusticeAreaId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<LocalJusticeAreaEntity> searchLocalJusticeAreas(LocalJusticeAreaSearchDto criteria) {
        log.info(":searchLocalJusticeAreas: criteria: {} via gateway {}", criteria.toJson(), legacyGateway.getUrl());
        return postToGateway("searchLocalJusticeAreas", LegacyLocalJusticeAreaSearchResults.class, criteria)
            .getLocalJusticeAreaEntities();
    }

}
