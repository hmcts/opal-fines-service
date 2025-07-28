package uk.gov.hmcts.opal.disco.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.ReportSearchDto;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.disco.ReportServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyReportService;
import uk.gov.hmcts.opal.disco.opal.ReportService;
import uk.gov.hmcts.opal.service.proxy.ProxyInterface;

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
