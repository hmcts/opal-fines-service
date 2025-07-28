package uk.gov.hmcts.opal.disco.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.ReportInstanceSearchDto;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.disco.ReportInstanceServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyReportInstanceService;
import uk.gov.hmcts.opal.disco.opal.ReportInstanceService;
import uk.gov.hmcts.opal.service.proxy.ProxyInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("reportInstanceServiceProxy")
public class ReportInstanceServiceProxy implements ReportInstanceServiceInterface, ProxyInterface {

    private final ReportInstanceService opalReportInstanceService;
    private final LegacyReportInstanceService legacyReportInstanceService;
    private final DynamicConfigService dynamicConfigService;

    private ReportInstanceServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyReportInstanceService : opalReportInstanceService;
    }

    @Override
    public ReportInstanceEntity getReportInstance(long reportInstanceId) {
        return getCurrentModeService().getReportInstance(reportInstanceId);
    }

    @Override
    public List<ReportInstanceEntity> searchReportInstances(ReportInstanceSearchDto criteria) {
        return getCurrentModeService().searchReportInstances(criteria);
    }
}
