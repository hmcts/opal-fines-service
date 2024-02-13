package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.dto.search.EnforcerSearchDto;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.service.EnforcerServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyEnforcerService")
public class LegacyEnforcerService extends LegacyService implements EnforcerServiceInterface {

    @Autowired
    protected LegacyEnforcerService(@Value("${legacy-gateway.url}") String gatewayUrl, RestClient restClient) {
        super(gatewayUrl, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public EnforcerEntity getEnforcer(long enforcerId) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<EnforcerEntity> searchEnforcers(EnforcerSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
