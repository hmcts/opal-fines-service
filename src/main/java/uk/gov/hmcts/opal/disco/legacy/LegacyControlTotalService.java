package uk.gov.hmcts.opal.disco.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.search.ControlTotalSearchDto;
import uk.gov.hmcts.opal.entity.ControlTotalEntity;
import uk.gov.hmcts.opal.disco.ControlTotalServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "opal.LegacyControlTotalService")
public class LegacyControlTotalService extends LegacyService implements ControlTotalServiceInterface {

    public LegacyControlTotalService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public ControlTotalEntity getControlTotal(long controlTotalId) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<ControlTotalEntity> searchControlTotals(ControlTotalSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
