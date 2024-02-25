package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.search.PrisonSearchDto;
import uk.gov.hmcts.opal.entity.PrisonEntity;
import uk.gov.hmcts.opal.service.PrisonServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyPrisonService")
public class LegacyPrisonService extends LegacyService implements PrisonServiceInterface {

    public LegacyPrisonService(LegacyGatewayProperties legacyGateway, RestClient restClient) {
        super(legacyGateway, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public PrisonEntity getPrison(long prisonId) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<PrisonEntity> searchPrisons(PrisonSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
