package uk.gov.hmcts.opal.disco.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.ReportEntrySearchDto;
import uk.gov.hmcts.opal.entity.ReportEntryEntity;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.disco.ReportEntryServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyReportEntryService;
import uk.gov.hmcts.opal.disco.opal.ReportEntryService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("reportEntryServiceProxy")
public class ReportEntryServiceProxy implements ReportEntryServiceInterface, ProxyInterface {

    private final ReportEntryService opalReportEntryService;
    private final LegacyReportEntryService legacyReportEntryService;
    private final DynamicConfigService dynamicConfigService;

    private ReportEntryServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyReportEntryService : opalReportEntryService;
    }

    @Override
    public ReportEntryEntity getReportEntry(long reportEntryId) {
        return getCurrentModeService().getReportEntry(reportEntryId);
    }

    @Override
    public List<ReportEntryEntity> searchReportEntries(ReportEntrySearchDto criteria) {
        return getCurrentModeService().searchReportEntries(criteria);
    }
}
