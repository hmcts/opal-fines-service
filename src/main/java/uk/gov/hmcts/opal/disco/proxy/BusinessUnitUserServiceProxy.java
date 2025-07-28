package uk.gov.hmcts.opal.disco.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.BusinessUnitUserSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitUserEntity;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.disco.BusinessUnitUserServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyBusinessUnitUserService;
import uk.gov.hmcts.opal.disco.opal.BusinessUnitUserService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("businessUnitUserServiceProxy")
public class BusinessUnitUserServiceProxy implements BusinessUnitUserServiceInterface, ProxyInterface {

    private final BusinessUnitUserService opalBusinessUnitUserService;
    private final LegacyBusinessUnitUserService legacyBusinessUnitUserService;
    private final DynamicConfigService dynamicConfigService;

    private BusinessUnitUserServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyBusinessUnitUserService : opalBusinessUnitUserService;
    }

    @Override
    public BusinessUnitUserEntity getBusinessUnitUser(String businessUnitUserId) {
        return getCurrentModeService().getBusinessUnitUser(businessUnitUserId);
    }

    @Override
    public List<BusinessUnitUserEntity> searchBusinessUnitUsers(BusinessUnitUserSearchDto criteria) {
        return getCurrentModeService().searchBusinessUnitUsers(criteria);
    }
}
