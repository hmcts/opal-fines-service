package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.LogActionSearchDto;
import uk.gov.hmcts.opal.entity.LogActionEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.LogActionServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyLogActionService;
import uk.gov.hmcts.opal.service.opal.LogActionService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("logActionServiceProxy")
public class LogActionServiceProxy implements LogActionServiceInterface, ProxyInterface {

    private final LogActionService opalLogActionService;
    private final LegacyLogActionService legacyLogActionService;
    private final DynamicConfigService dynamicConfigService;

    private LogActionServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyLogActionService : opalLogActionService;
    }

    @Override
    public LogActionEntity getLogAction(short logActionId) {
        return getCurrentModeService().getLogAction(logActionId);
    }

    @Override
    public List<LogActionEntity> searchLogActions(LogActionSearchDto criteria) {
        return getCurrentModeService().searchLogActions(criteria);
    }
}
