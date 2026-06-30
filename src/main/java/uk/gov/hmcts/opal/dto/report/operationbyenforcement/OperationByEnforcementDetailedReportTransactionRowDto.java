package uk.gov.hmcts.opal.dto.report.operationbyenforcement;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationByEnforcementDetailedReportTransactionRowDto {

    private String accountNo;
    private String consolidatedAccountNo;
    private LocalDate transactionDate;
    private String transactionType;
    private String transactionDetails;
    private String transactionUserId;
    private BigDecimal transactionAmount;

}