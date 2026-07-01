package uk.gov.hmcts.opal.dto.report.operation;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryReportTotalsRowDto {

    private Integer accountsReported;   // count
    private BigDecimal totalImposed;    // sum of amount_imposed
    private BigDecimal totalPaid;       // sum of amount_paid
    private BigDecimal totalBalance;    // sum of account_balance
}