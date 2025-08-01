package uk.gov.hmcts.opal.disco.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.ApplicationFunctionSearchDto;
import uk.gov.hmcts.opal.entity.ApplicationFunctionEntity;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.disco.ApplicationFunctionServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyApplicationFunctionService;
import uk.gov.hmcts.opal.disco.opal.ApplicationFunctionService;
import uk.gov.hmcts.opal.service.proxy.ProxyInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("applicationFunctionServiceProxy")
public class ApplicationFunctionServiceProxy implements ApplicationFunctionServiceInterface, ProxyInterface {

    private final ApplicationFunctionService opalApplicationFunctionService;
    private final LegacyApplicationFunctionService legacyApplicationFunctionService;
    private final DynamicConfigService dynamicConfigService;

    private ApplicationFunctionServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyApplicationFunctionService : opalApplicationFunctionService;
    }

    @Override
    public ApplicationFunctionEntity getApplicationFunction(long applicationFunctionId) {
        return getCurrentModeService().getApplicationFunction(applicationFunctionId);
    }

    @Override
    public List<ApplicationFunctionEntity> searchApplicationFunctions(ApplicationFunctionSearchDto criteria) {
        return getCurrentModeService().searchApplicationFunctions(criteria);
    }
}
