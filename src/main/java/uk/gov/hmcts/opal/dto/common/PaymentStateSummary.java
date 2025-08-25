package uk.gov.hmcts.opal.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
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
