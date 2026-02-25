package uk.gov.hmcts.opal.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentStateSummary {

    @JsonProperty("imposed_amount")
    private BigDecimal imposedAmount;

    @JsonProperty("arrears_amount")
    private BigDecimal arrearsAmount;

    @JsonProperty("paid_amount")
    private BigDecimal paidAmount;

    @JsonProperty("account_balance")
    private BigDecimal accountBalance;
}
