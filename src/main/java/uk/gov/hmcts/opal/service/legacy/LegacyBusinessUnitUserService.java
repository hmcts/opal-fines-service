package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.searchResults.LegacyBusinessUnitUserSearchResults;
import uk.gov.hmcts.opal.dto.search.BusinessUnitUserSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitUserEntity;
import uk.gov.hmcts.opal.service.BusinessUnitUserServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyBusinessUnitUserService")
public class LegacyBusinessUnitUserService extends LegacyService implements BusinessUnitUserServiceInterface {

    public LegacyBusinessUnitUserService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public BusinessUnitUserEntity getBusinessUnitUser(String businessUnitUserId) {
        log.info("getBusinessUnitUser for {} from {}", businessUnitUserId, legacyGateway.getUrl());
        return postToGateway("getBusinessUnitUser", BusinessUnitUserEntity.class, businessUnitUserId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<BusinessUnitUserEntity> searchBusinessUnitUsers(BusinessUnitUserSearchDto criteria) {
        log.info(":searchBusinessUnitUsers: criteria: {} via gateway {}", criteria.toJson(), legacyGateway.getUrl());
        return postToGateway("searchBusinessUnitUsers", LegacyBusinessUnitUserSearchResults.class, criteria)
            .getBusinessUnitUserEntities();
    }

}
