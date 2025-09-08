package uk.gov.hmcts.opal.dto.legacy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LegacyPaymentTermsType {

    private PaymentTermsTypeCode paymentTermsTypeCode;

    public enum PaymentTermsTypeCode {
        B,
        P,
        I
    }

}
