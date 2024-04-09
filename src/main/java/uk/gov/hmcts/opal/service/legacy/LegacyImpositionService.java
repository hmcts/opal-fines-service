package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.search.ImpositionSearchDto;
import uk.gov.hmcts.opal.entity.ImpositionEntity;
import uk.gov.hmcts.opal.service.ImpositionServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyImpositionService")
public class LegacyImpositionService extends LegacyService implements ImpositionServiceInterface {

    public LegacyImpositionService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public ImpositionEntity getImposition(long impositionId) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<ImpositionEntity> searchImpositions(ImpositionSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
