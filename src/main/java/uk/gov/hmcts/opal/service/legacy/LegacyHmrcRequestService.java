package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.search.HmrcRequestSearchDto;
import uk.gov.hmcts.opal.entity.HmrcRequestEntity;
import uk.gov.hmcts.opal.service.HmrcRequestServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyHmrcRequestService")
public class LegacyHmrcRequestService extends LegacyService implements HmrcRequestServiceInterface {

    public LegacyHmrcRequestService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public HmrcRequestEntity getHmrcRequest(long hmrcRequestId) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<HmrcRequestEntity> searchHmrcRequests(HmrcRequestSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
