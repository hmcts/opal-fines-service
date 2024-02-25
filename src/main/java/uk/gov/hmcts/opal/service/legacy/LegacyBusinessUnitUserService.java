package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
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
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<BusinessUnitUserEntity> searchBusinessUnitUsers(BusinessUnitUserSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
