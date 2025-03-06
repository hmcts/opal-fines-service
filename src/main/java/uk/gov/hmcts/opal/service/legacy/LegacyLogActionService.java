package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyLogActionSearchResults;
import uk.gov.hmcts.opal.dto.search.LogActionSearchDto;
import uk.gov.hmcts.opal.entity.LogActionEntity;
import uk.gov.hmcts.opal.service.LogActionServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "opal.LegacyLogActionService")
public class LegacyLogActionService extends LegacyService implements LogActionServiceInterface {

    public LegacyLogActionService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public LogActionEntity getLogAction(short logActionId) {
        log.debug("getLogAction for {} from {}", logActionId, legacyGateway.getUrl());
        return postToGateway("getLogAction", LogActionEntity.class, logActionId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<LogActionEntity> searchLogActions(LogActionSearchDto criteria) {
        log.debug(":searchLogActions: criteria: {} via gateway {}", criteria.toJson(), legacyGateway.getUrl());
        return postToGateway("searchLogActions", LegacyLogActionSearchResults.class, criteria)
            .getLogActionEntities();
    }

}
