package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.search.LogActionSearchDto;
import uk.gov.hmcts.opal.entity.LogActionEntity;
import uk.gov.hmcts.opal.service.LogActionServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyLogActionService")
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
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<LogActionEntity> searchLogActions(LogActionSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
