package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.search.ResultSearchDto;
import uk.gov.hmcts.opal.entity.ResultEntity;
import uk.gov.hmcts.opal.service.ResultServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyResultService")
public class LegacyResultService extends LegacyService implements ResultServiceInterface {

    public LegacyResultService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public ResultEntity getResult(long resultId) {
        log.info("getResult for {} from {}", resultId, legacyGateway.getUrl());
        return postToGateway("getResult", ResultEntity.class, resultId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ResultEntity> searchResults(ResultSearchDto criteria) {
        log.info(":searchResults: criteria: {} via gateway {}", criteria.toJson(), legacyGateway.getUrl());
        return postToGateway("searchResults", List.class, criteria);
    }

}
