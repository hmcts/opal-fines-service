package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.search.CourtFeeSearchDto;
import uk.gov.hmcts.opal.entity.CourtFeeEntity;
import uk.gov.hmcts.opal.service.CourtFeeServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyCourtFeeService")
public class LegacyCourtFeeService extends LegacyService implements CourtFeeServiceInterface {

    public LegacyCourtFeeService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public CourtFeeEntity getCourtFee(long courtFeeId) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<CourtFeeEntity> searchCourtFees(CourtFeeSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
