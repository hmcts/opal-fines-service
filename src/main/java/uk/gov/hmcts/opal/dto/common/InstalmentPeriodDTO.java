package uk.gov.hmcts.opal.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstalmentPeriodDTO {

    @JsonProperty("instalment_period_code")
    private String instalmentPeriodCode;

    @JsonProperty("instalment_period_display_name")
    private String instalmentPeriodDisplayName; // always derived from instalmentPeriodCode

    public InstalmentPeriodDTO(InstalmentPeriod period) {
        if (period != null) {
            this.instalmentPeriodCode = period.getInstalmentPeriodCode();
            this.instalmentPeriodDisplayName = period.getInstalmentPeriodDisplayName();
        }
    }

    public static InstalmentPeriodDTO ofCode(String code) {
        InstalmentPeriod period = InstalmentPeriod.fromCode(code);
        return new InstalmentPeriodDTO(period);
    }

    /** Setting the code automatically derives the display name. */
    public void setInstalmentPeriodCode(String instalmentPeriodCode) {
        this.instalmentPeriodCode = instalmentPeriodCode;
        InstalmentPeriod period = InstalmentPeriod.fromCode(instalmentPeriodCode);
        this.instalmentPeriodDisplayName = period != null ? period.getInstalmentPeriodDisplayName() : null;
    }
}
