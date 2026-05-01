package uk.gov.hmcts.opal.service.report;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EnforcementReportDto {

    List<EnforcementReportRowDto> transactionList;

    EnforcementReportTotalsRowDto totals;
}
