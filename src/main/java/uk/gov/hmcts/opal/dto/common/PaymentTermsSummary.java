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

    @JsonProperty("payment_terms_type")
    private PaymentTermsType paymentTermsType;

    @JsonProperty("effective_date")
    private LocalDate effectiveDate;

    @JsonProperty("instalment_period")
    private InstalmentPeriod instalmentPeriod;

    @JsonProperty("lump_sum_amount")
    private BigDecimal lumpSumAmount;

    @JsonProperty("instalment_amount")
    private BigDecimal instalmentAmount;
}
