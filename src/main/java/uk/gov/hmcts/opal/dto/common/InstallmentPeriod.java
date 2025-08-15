package uk.gov.hmcts.opal.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstallmentPeriod  implements ToJsonString {

    @JsonProperty("installment_period_code")
    private String installmentPeriodCode;

    @JsonProperty("installment_period_display_name")
    private String installmentPeriodDisplayName;
}
