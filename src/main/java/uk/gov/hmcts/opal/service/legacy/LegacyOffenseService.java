package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.search.OffenseSearchDto;
import uk.gov.hmcts.opal.entity.OffenseEntity;
import uk.gov.hmcts.opal.service.OffenseServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyOffenseService")
public class LegacyOffenseService extends LegacyService implements OffenseServiceInterface {

    public LegacyOffenseService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public OffenseEntity getOffense(long offenseId) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<OffenseEntity> searchOffenses(OffenseSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
