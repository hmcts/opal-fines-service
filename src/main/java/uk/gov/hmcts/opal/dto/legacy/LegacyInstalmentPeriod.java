package uk.gov.hmcts.opal.dto.legacy;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "installment_period")
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyInstalmentPeriod {

    @XmlElement(name = "installment_period_code")
    private InstalmentPeriodCode instalmentPeriodCode;

    public enum InstalmentPeriodCode {
        W,
        M,
        F
    }
}
