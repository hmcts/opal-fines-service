package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.ReportSearchDto;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.ReportServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyReportService;
import uk.gov.hmcts.opal.service.opal.ReportService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("reportServiceProxy")
public class ReportServiceProxy implements ReportServiceInterface, ProxyInterface {

    private final ReportService opalReportService;
    private final LegacyReportService legacyReportService;
    private final DynamicConfigService dynamicConfigService;

    private ReportServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyReportService : opalReportService;
    }

    @Override
    public ReportEntity getReport(long reportId) {
        return getCurrentModeService().getReport(reportId);
    }

    @Override
    public List<ReportEntity> searchReports(ReportSearchDto criteria) {
        return getCurrentModeService().searchReports(criteria);
    }
}
