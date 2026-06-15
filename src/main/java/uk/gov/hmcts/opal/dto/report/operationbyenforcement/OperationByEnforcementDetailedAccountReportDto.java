package uk.gov.hmcts.opal.dto.report.operationbyenforcement;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OperationByEnforcementDetailedAccountReportDto {

    OperationByEnforcementDetailedReportAccountRowDto accountRow;

    List<OperationByEnforcementDetailedReportTransactionRowDto> transactionRows;

}
