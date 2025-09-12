package uk.gov.hmcts.opal.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToJsonString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTermsSummary implements ToJsonString {

    @JsonProperty("payment_terms")
    private PaymentTerms paymentTerms;

    @JsonProperty("effective_date")
    private LocalDate effectiveDate;

    @JsonProperty("installment_period")
    private String installmentPeriod;

    @JsonProperty("lump_sum_amount")
    private BigDecimal lumpSumAmount;

    @JsonProperty("installment_amount")
    private BigDecimal installmentAmount;
}
