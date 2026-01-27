package uk.gov.hmcts.opal.dto.legacy.common;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.Payment;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "payment", propOrder = { "isBacs", "holdPayment" })
public class LegacyPayment {

    @XmlElement(name = "is_bacs", required = true)
    private boolean isBacs;

    @XmlElement(name = "hold_payment", required = true)
    private boolean holdPayment;

    public Payment toOpalDto() {
        return Payment.builder()
            .isBacs(this.isBacs())
            .holdPayment(this.isHoldPayment())
            .build();
    }
}
