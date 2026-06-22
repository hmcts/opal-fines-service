package uk.gov.hmcts.opal.service.report;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CashTillReportData implements ReportDataInterface {

    @Builder.Default
    private List<CashTillReportRow> rows = List.of();

    @Builder.Default
    private Boolean allocatedReport = false;

    @Builder.Default
    private ReportMetaData reportMetaData = new ReportMetaData(List.of());

    @Override
    @JsonIgnore
    public long getNumberOfRecords() {
        return rows == null ? 0 : rows.size();
    }

    @Override
    @JsonIgnore
    public ReportMetaData getReportMetaData() {
        return reportMetaData == null ? new ReportMetaData(List.of()) : reportMetaData;
    }
}
