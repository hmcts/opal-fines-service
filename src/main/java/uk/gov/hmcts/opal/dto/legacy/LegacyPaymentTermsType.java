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
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyPaymentTermsType {

    @XmlElement(name = "payment_terms_type_code")
    private PaymentTermsTypeCode paymentTermsTypeCode;

    public enum PaymentTermsTypeCode {
        B,
        P,
        I
    }

}
