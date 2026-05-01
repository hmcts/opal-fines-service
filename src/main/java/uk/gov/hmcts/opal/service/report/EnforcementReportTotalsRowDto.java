package uk.gov.hmcts.opal.service.report;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnforcementReportTotalsRowDto {

    private Integer accountsReported;  // count
    private BigDecimal totalImposed;    // sum of amount_imposed
    private BigDecimal totalPaid;       // sum of amount_paid
    private BigDecimal totalBalance;    // sum of account_balance
}