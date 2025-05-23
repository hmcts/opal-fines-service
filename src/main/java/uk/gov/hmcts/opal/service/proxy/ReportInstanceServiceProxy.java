package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.ReportInstanceSearchDto;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.ReportInstanceServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyReportInstanceService;
import uk.gov.hmcts.opal.service.opal.ReportInstanceService;

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
