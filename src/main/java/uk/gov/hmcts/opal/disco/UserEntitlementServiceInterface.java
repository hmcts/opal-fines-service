package uk.gov.hmcts.opal.disco;

import uk.gov.hmcts.opal.dto.search.UserEntitlementSearchDto;
import uk.gov.hmcts.opal.entity.UserEntitlementEntity;

import java.util.List;

public interface UserEntitlementServiceInterface {

    UserEntitlementEntity getUserEntitlement(long userEntitlementId);

    List<UserEntitlementEntity> searchUserEntitlements(UserEntitlementSearchDto criteria);
}
