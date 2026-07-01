package uk.gov.hmcts.opal.dto.report.operation;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SummaryReportDto {

    List<SummaryOperationReportRowDto> reportSummaryRows;

    SummaryReportTotalsRowDto totals;
}
