package uk.gov.hmcts.opal.dto.legacy.common;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentStateSummary {

    @XmlElement(name = "imposed_amount")
    private String imposedAmount;

    @XmlElement(name = "arrears_amount")
    private String arrearsAmount;

    @XmlElement(name = "paid_amount")
    private String paidAmount;

    @XmlElement(name = "account_balance")
    private String accountBalance;
}
