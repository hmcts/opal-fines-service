package uk.gov.hmcts.opal.disco;

import uk.gov.hmcts.opal.dto.search.ReportEntrySearchDto;
import uk.gov.hmcts.opal.entity.ReportEntryEntity;

import java.util.List;

public interface ReportEntryServiceInterface {

    ReportEntryEntity getReportEntry(long reportEntryId);

    List<ReportEntryEntity> searchReportEntries(ReportEntrySearchDto criteria);
}
