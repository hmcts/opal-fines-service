package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyBusinessUnitSearchResults;
import uk.gov.hmcts.opal.dto.search.BusinessUnitSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.service.BusinessUnitServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "opal.LegacyBusinessUnitService")
public class LegacyBusinessUnitService extends LegacyService implements BusinessUnitServiceInterface {


    public LegacyBusinessUnitService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public BusinessUnitEntity getBusinessUnit(short businessUnitId) {
        log.debug("getBusinessUnit for {} from {}", businessUnitId, legacyGateway.getUrl());
        return postToGateway("getBusinessUnit", BusinessUnitEntity.class, businessUnitId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<BusinessUnitEntity> searchBusinessUnits(BusinessUnitSearchDto criteria) {
        log.debug(":searchBusinessUnits: criteria: {} via gateway {}", criteria.toJson(), legacyGateway.getUrl());
        return postToGateway("searchBusinessUnits", LegacyBusinessUnitSearchResults.class, criteria)
            .getBusinessUnitEntities();
    }

}
