package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.dto.search.TillSearchDto;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.service.TillServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyTillService")
public class LegacyTillService extends LegacyService implements TillServiceInterface {

    @Autowired
    protected LegacyTillService(@Value("${legacy-gateway-url}") String gatewayUrl, RestClient restClient) {
        super(gatewayUrl, restClient);
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
