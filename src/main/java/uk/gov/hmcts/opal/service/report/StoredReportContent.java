package uk.gov.hmcts.opal.service.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
class StoredReportContent {

    private Object reportData;
    private ReportMetaData reportMetaData;
}
