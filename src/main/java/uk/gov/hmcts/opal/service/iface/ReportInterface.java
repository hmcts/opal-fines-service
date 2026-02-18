package uk.gov.hmcts.opal.service.iface;

import groovy.io.FileType;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;

public interface ReportInterface<T extends ReportDataInterface> {

    T generateReportData(ReportInstanceEntity reportInstance);

    byte[] convertReportDataToFileType(ReportInstanceEntity reportInstance, T reportData, FileType fileType);
}

