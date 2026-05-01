package uk.gov.hmcts.opal.dto.report;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EnforcementReportDto {

    List<EnforcementReportRowDto> transactionList;

    EnforcementReportTotalsRowDto totals;
}
