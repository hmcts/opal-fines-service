package uk.gov.hmcts.opal.service.report;

import uk.gov.hmcts.opal.entity.ReportInstanceEntity;

public interface ReportInterface<T extends ReportDataInterface> {

    ReportId getReportId();

    T generateReportData(ReportInstanceEntity reportInstance);

    Class<? extends T> getStoredReportDataClass(ReportInstanceEntity reportInstance);

    default void validateReportDataForFileType(FileType fileType, T reportData) {
        // Report implementations may restrict which report data variants support a file type.
    }

    byte[] convertReportDataToFileType(ReportInstanceEntity reportInstance, T reportData, FileType fileType);
}
