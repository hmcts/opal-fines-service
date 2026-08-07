package uk.gov.hmcts.opal.service.report.operation;

import java.util.List;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.exception.UnsupportedContentTypeException;
import uk.gov.hmcts.opal.service.report.FileType;
import uk.gov.hmcts.opal.service.report.ReportCSVService;
import uk.gov.hmcts.opal.service.report.ReportInterface;

public abstract class AbstractOperationReportService implements ReportInterface<OperationReportDataInterface> {

    private final ReportCSVService reportCSVService;

    protected AbstractOperationReportService(ReportCSVService reportCSVService) {
        this.reportCSVService = reportCSVService;
    }

    @Override
    public byte[] convertReportDataToFileType(ReportInstanceEntity reportInstance,
        OperationReportDataInterface reportData, FileType fileType) {

        if (fileType != FileType.CSV) {
            throw new UnsupportedContentTypeException(
                getReportId().getReportId(), fileType.name(), List.of(FileType.CSV.name()));
        }
        if (!(reportData instanceof OperationSummaryReport summaryReport)) {
            throw new UnsupportedContentTypeException(
                getReportId().getReportId(), "DETAILED CSV", List.of("SUMMARY CSV"));
        }
        return reportCSVService.convertReportDtoToCSV(summaryReport);
    }
}
