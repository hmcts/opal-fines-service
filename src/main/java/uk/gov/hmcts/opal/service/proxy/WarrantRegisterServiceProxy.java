package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.WarrantRegisterSearchDto;
import uk.gov.hmcts.opal.entity.WarrantRegisterEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.WarrantRegisterServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyWarrantRegisterService;
import uk.gov.hmcts.opal.service.opal.WarrantRegisterService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("warrantRegisterServiceProxy")
public class WarrantRegisterServiceProxy implements WarrantRegisterServiceInterface, ProxyInterface {

    private final WarrantRegisterService opalWarrantRegisterService;
    private final LegacyWarrantRegisterService legacyWarrantRegisterService;
    private final DynamicConfigService dynamicConfigService;

    private WarrantRegisterServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyWarrantRegisterService : opalWarrantRegisterService;
    }

    @Override
    public WarrantRegisterEntity getWarrantRegister(long warrantRegisterId) {
        return getCurrentModeService().getWarrantRegister(warrantRegisterId);
    }

    @Override
    public List<WarrantRegisterEntity> searchWarrantRegisters(WarrantRegisterSearchDto criteria) {
        return getCurrentModeService().searchWarrantRegisters(criteria);
    }
}