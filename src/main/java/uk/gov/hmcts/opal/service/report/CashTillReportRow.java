package uk.gov.hmcts.opal.service.report;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CashTillReportRow {

    private String businessUnit;
    private String cashTillNumber;
    private String cashier;
    private LocalDateTime paymentDateTime;
    private CashTillDestinationType destinationType;
    private String details;
    private Boolean autoPayment;
    private CashTillPaymentMethod paymentMethod;
    private BigDecimal amount;
    private Boolean receipt;
    private BigDecimal balance;
    private Boolean allocated;
}
