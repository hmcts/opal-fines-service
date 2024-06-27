package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyDebtorDetailSearchResults;
import uk.gov.hmcts.opal.dto.search.DebtorDetailSearchDto;
import uk.gov.hmcts.opal.entity.DebtorDetailEntity;
import uk.gov.hmcts.opal.service.DebtorDetailServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyDebtorDetailService")
public class LegacyDebtorDetailService extends LegacyService implements DebtorDetailServiceInterface {


    public LegacyDebtorDetailService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public DebtorDetailEntity getDebtorDetail(long debtorDetailId) {
        log.info("getDebtorDetail for {} from {}", debtorDetailId, legacyGateway.getUrl());
        return postToGateway("getDebtorDetail", DebtorDetailEntity.class, debtorDetailId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DebtorDetailEntity> searchDebtorDetails(DebtorDetailSearchDto criteria) {
        log.info(":searchDebtorDetails: criteria: {} via gateway {}", criteria.toJson(), legacyGateway.getUrl());
        return postToGateway("searchDebtorDetails", LegacyDebtorDetailSearchResults.class, criteria)
            .getDebtorDetailEntities();
    }

}
