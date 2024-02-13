package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.dto.search.PrisonSearchDto;
import uk.gov.hmcts.opal.entity.PrisonEntity;
import uk.gov.hmcts.opal.service.PrisonServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyPrisonService")
public class LegacyPrisonService extends LegacyService implements PrisonServiceInterface {

    @Autowired
    protected LegacyPrisonService(@Value("${legacy-gateway.url}") String gatewayUrl, RestClient restClient) {
        super(gatewayUrl, restClient);
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
