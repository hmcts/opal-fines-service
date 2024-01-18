package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Data
class PaymentTermsDto {
    @JsonProperty("terms_date")
    private LocalDate termsDate;

    @JsonProperty("terms_type_code")
    private String termsTypeCode;

    @JsonProperty("instalment_amount")
    private BigDecimal instalmentAmount;

    @JsonProperty("instalment_period")
    private String instalmentPeriod;

    @JsonProperty("instalment_lump_sum")
    private BigDecimal instalmentLumpSum;

    @JsonProperty("jail_days")
    private Integer jailDays;

    @JsonProperty("wording")
    private String wording;

}
