package uk.gov.hmcts.opal.disco;

import uk.gov.hmcts.opal.dto.search.ReportSearchDto;
import uk.gov.hmcts.opal.entity.ReportEntity;

import java.util.List;

public interface ReportServiceInterface {

    ReportEntity getReport(long reportId);

    List<ReportEntity> searchReports(ReportSearchDto criteria);
}
