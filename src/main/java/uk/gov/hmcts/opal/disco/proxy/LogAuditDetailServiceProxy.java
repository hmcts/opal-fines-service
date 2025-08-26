package uk.gov.hmcts.opal.disco.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.LogAuditDetailSearchDto;
import uk.gov.hmcts.opal.entity.LogAuditDetailEntity;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.disco.LogAuditDetailServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyLogAuditDetailService;
import uk.gov.hmcts.opal.disco.opal.LogAuditDetailService;
import uk.gov.hmcts.opal.service.proxy.ProxyInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("logAuditDetailServiceProxy")
public class LogAuditDetailServiceProxy implements LogAuditDetailServiceInterface, ProxyInterface {

    private final LogAuditDetailService opalLogAuditDetailService;
    private final LegacyLogAuditDetailService legacyLogAuditDetailService;
    private final DynamicConfigService dynamicConfigService;

    private LogAuditDetailServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyLogAuditDetailService : opalLogAuditDetailService;
    }

    @Override
    public LogAuditDetailEntity getLogAuditDetail(long logAuditDetailId) {
        return getCurrentModeService().getLogAuditDetail(logAuditDetailId);
    }

    @Override
    public List<LogAuditDetailEntity> searchLogAuditDetails(LogAuditDetailSearchDto criteria) {
        return getCurrentModeService().searchLogAuditDetails(criteria);
    }
}
