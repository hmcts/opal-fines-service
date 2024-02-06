package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.dto.search.EnforcementSearchDto;
import uk.gov.hmcts.opal.entity.EnforcementEntity;
import uk.gov.hmcts.opal.service.EnforcementServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyEnforcementService")
public class LegacyEnforcementService extends LegacyService implements EnforcementServiceInterface {

    @Autowired
    protected LegacyEnforcementService(@Value("${legacy-gateway-url}") String gatewayUrl, RestClient restClient) {
        super(gatewayUrl, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public EnforcementEntity getEnforcement(long enforcementId) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<EnforcementEntity> searchEnforcements(EnforcementSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
