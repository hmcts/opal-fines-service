package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.search.TillSearchDto;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.service.TillServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyTillService")
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
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<TillEntity> searchTills(TillSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
