package uk.gov.hmcts.opal.disco.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.ControlTotalSearchDto;
import uk.gov.hmcts.opal.entity.ControlTotalEntity;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.disco.ControlTotalServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyControlTotalService;
import uk.gov.hmcts.opal.disco.opal.ControlTotalService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("controlTotalServiceProxy")
public class ControlTotalServiceProxy implements ControlTotalServiceInterface, ProxyInterface {

    private final ControlTotalService opalControlTotalService;
    private final LegacyControlTotalService legacyControlTotalService;
    private final DynamicConfigService dynamicConfigService;

    private ControlTotalServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyControlTotalService : opalControlTotalService;
    }

    @Override
    public ControlTotalEntity getControlTotal(long controlTotalId) {
        return getCurrentModeService().getControlTotal(controlTotalId);
    }

    @Override
    public List<ControlTotalEntity> searchControlTotals(ControlTotalSearchDto criteria) {
        return getCurrentModeService().searchControlTotals(criteria);
    }
}
