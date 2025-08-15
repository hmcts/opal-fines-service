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
public class PaymentTerms  implements ToJsonString {

    @JsonProperty("days_in_default")
    private Integer daysInDefault;

    @JsonProperty("days_in_default_imposed")
    private LocalDate daysInDefaultImposed;

    @JsonProperty("reason_for_extension")
    private String reasonForExtension;

    // nested?
    @JsonProperty("payment_terms")
    private PaymentTerms paymentTerms;

    @JsonProperty("effective_date")
    private LocalDate effectiveDate;

    @JsonProperty("installment_period")
    private InstallmentPeriod installmentPeriod;

    @JsonProperty("lump_sum_amount")
    private BigDecimal lumpSumAmount;

    @JsonProperty("installment_amount")
    private BigDecimal installmentAmount;
}
