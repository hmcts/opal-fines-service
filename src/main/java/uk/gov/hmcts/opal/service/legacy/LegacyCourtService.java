package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.searchResults.LegacyCourtSearchResults;
import uk.gov.hmcts.opal.dto.search.CourtSearchDto;
import uk.gov.hmcts.opal.entity.CourtEntity;
import uk.gov.hmcts.opal.service.CourtServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyCourtService")
public class LegacyCourtService extends LegacyService implements CourtServiceInterface {


    public LegacyCourtService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public CourtEntity getCourt(long courtId) {
        log.info("getCourt for {} from {}", courtId, legacyGateway.getUrl());
        return postToGateway("getCourt", CourtEntity.class, courtId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<CourtEntity> searchCourts(CourtSearchDto criteria) {
        log.info(":searchCourts: criteria: {} via gateway {}", criteria.toJson(), legacyGateway.getUrl());
        return postToGateway("searchCourts", LegacyCourtSearchResults.class, criteria)
            .getCourtEntities();
    }

}
