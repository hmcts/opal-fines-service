package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.search.UserEntitlementSearchDto;
import uk.gov.hmcts.opal.entity.UserEntitlementEntity;
import uk.gov.hmcts.opal.service.UserEntitlementServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyUserEntitlementService")
public class LegacyUserEntitlementService extends LegacyService implements UserEntitlementServiceInterface {

    public LegacyUserEntitlementService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public UserEntitlementEntity getUserEntitlement(long userEntitlementId) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<UserEntitlementEntity> searchUserEntitlements(UserEntitlementSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
