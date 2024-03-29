package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.UserEntitlementSearchDto;
import uk.gov.hmcts.opal.entity.UserEntitlementEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.UserEntitlementServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyUserEntitlementService;
import uk.gov.hmcts.opal.service.opal.UserEntitlementService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("userEntitlementServiceProxy")
public class UserEntitlementServiceProxy implements UserEntitlementServiceInterface, ProxyInterface {

    private final UserEntitlementService opalUserEntitlementService;
    private final LegacyUserEntitlementService legacyUserEntitlementService;
    private final DynamicConfigService dynamicConfigService;

    private UserEntitlementServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyUserEntitlementService : opalUserEntitlementService;
    }

    @Override
    public UserEntitlementEntity getUserEntitlement(long userEntitlementId) {
        return getCurrentModeService().getUserEntitlement(userEntitlementId);
    }

    @Override
    public List<UserEntitlementEntity> searchUserEntitlements(UserEntitlementSearchDto criteria) {
        return getCurrentModeService().searchUserEntitlements(criteria);
    }
}
