package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;

@Service
@Slf4j(topic = "LegacyTestingSupportService")
public class LegacyTestingSupportService extends LegacyService {

    public LegacyTestingSupportService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    public Logger getLog() {
        return log;
    }

    public ResponseEntity<String> postLegacyFunction(String actionType, Object request) {
        return super.postToGatewayRawResponse(actionType, request);
    }
}
