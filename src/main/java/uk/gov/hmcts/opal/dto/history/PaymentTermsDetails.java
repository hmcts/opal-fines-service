package uk.gov.hmcts.opal.dto.history;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.common.InstalmentPeriod;
import uk.gov.hmcts.opal.dto.common.PaymentTermsType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentTermsDetails {

    @JsonProperty("days_in_default")
    private Integer daysInDefault;

    @JsonProperty("date_days_in_default_imposed")
    private LocalDate dateDaysInDefaultImposed;

    @JsonProperty("reason_for_extension")
    private String reasonForExtension;

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
