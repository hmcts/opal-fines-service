package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyCourtFeeSearchResults;
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
        log.info("getCourtFee for {} from {}", courtFeeId, legacyGateway.getUrl());
        return postToGateway("getCourtFee", CourtFeeEntity.class, courtFeeId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<CourtFeeEntity> searchCourtFees(CourtFeeSearchDto criteria) {
        log.info(":searchCourtFees: criteria: {} via gateway {}", criteria.toJson(), legacyGateway.getUrl());
        return postToGateway("searchCourtFees", LegacyCourtFeeSearchResults.class, criteria)
            .getCourtFeeEntities();
    }

}
