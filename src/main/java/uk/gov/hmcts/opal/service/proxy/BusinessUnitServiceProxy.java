package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.BusinessUnitSearchDto;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnit;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitCore;
import uk.gov.hmcts.opal.service.BusinessUnitServiceInterface;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.legacy.LegacyBusinessUnitService;
import uk.gov.hmcts.opal.service.opal.BusinessUnitService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("businessUnitServiceProxy")
public class BusinessUnitServiceProxy implements BusinessUnitServiceInterface, ProxyInterface {

    private final BusinessUnitService opalBusinessUnitService;
    private final LegacyBusinessUnitService legacyBusinessUnitService;
    private final DynamicConfigService dynamicConfigService;

    private BusinessUnitServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyBusinessUnitService : opalBusinessUnitService;
    }

    @Override
    public BusinessUnitCore getBusinessUnit(short businessUnitId) {
        return getCurrentModeService().getBusinessUnit(businessUnitId);
    }

    @Override
    public List<BusinessUnit.Lite> searchBusinessUnits(BusinessUnitSearchDto criteria) {
        return getCurrentModeService().searchBusinessUnits(criteria);
    }
}
