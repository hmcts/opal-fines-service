package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.search.AmendmentSearchDto;
import uk.gov.hmcts.opal.entity.AmendmentEntity;
import uk.gov.hmcts.opal.service.AmendmentServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyAmendmentService")
public class LegacyAmendmentService extends LegacyService implements AmendmentServiceInterface {

    public LegacyAmendmentService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public AmendmentEntity getAmendment(long amendmentId) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<AmendmentEntity> searchAmendments(AmendmentSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}