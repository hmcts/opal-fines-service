package uk.gov.hmcts.opal.dto.legacy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LegacyInstalmentPeriod {

    private InstalmentPeriodCode instalmentPeriodCode;

    public enum InstalmentPeriodCode {
        W,
        M,
        F
    }
}
