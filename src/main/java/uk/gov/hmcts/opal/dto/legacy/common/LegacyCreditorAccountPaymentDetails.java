package uk.gov.hmcts.opal.dto.legacy.common;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyCreditorAccountPaymentDetails {

    @XmlElement(name = "account_name")
    private String accountName;

    @XmlElement(name = "sort_code")
    private String sortCode;

    @XmlElement(name = "account_number")
    private String accountNumber;

    @XmlElement(name = "account_reference")
    private String accountReference;

    @XmlElement(name = "pay_by_bacs", required = true)
    private Boolean payByBacs;

    @XmlElement(name = "hold_payment", required = true)
    private Boolean holdPayment;
}
