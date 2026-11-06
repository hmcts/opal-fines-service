package uk.gov.hmcts.opal.service.report;

import java.time.LocalDateTime;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PdfReportData<T extends ReportDataInterface>(
    LocalDateTime createdAt,
    String createdBy,
    Long numberOfRecords,
    T reportData) {

    public static <T extends ReportDataInterface> PdfReportData<T> from(ReportInstanceEntity instance, T reportData) {
        return new PdfReportData<>(
            instance.getCreatedTimestamp(),
            instance.getRequestedByName(),
            instance.getNoOfRecords(),
            reportData);
    }
}
