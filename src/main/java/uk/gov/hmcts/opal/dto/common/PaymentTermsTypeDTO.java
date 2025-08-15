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
public class PaymentTermsTypeDTO {

    @JsonProperty("payment_terms_type_code")
    private String paymentTermsTypeCode;

    @JsonProperty("payment_terms_type_display_name")
    private String paymentTermsTypeDisplayName; // always derived from paymentTermsTypeCode

    public PaymentTermsTypeDTO(PaymentTermsType paymentTermsType) {
        if (paymentTermsType != null) {
            this.paymentTermsTypeCode = paymentTermsType.getPaymentTermsTypeCode();
            this.paymentTermsTypeDisplayName = paymentTermsType.getPaymentTermsTypeDisplayName();
        }
    }

    public static PaymentTermsTypeDTO ofCode(String code) {
        PaymentTermsType paymentTermsType = PaymentTermsType.fromCode(code);
        return new PaymentTermsTypeDTO(paymentTermsType);
    }

    /** Setting the code automatically derives the display name. */
    public void setPaymentTermsTypeCode(String paymentTermsTypeCode) {
        this.paymentTermsTypeCode = paymentTermsTypeCode;
        PaymentTermsType paymentTermsType = PaymentTermsType.fromCode(paymentTermsTypeCode);
        this.paymentTermsTypeDisplayName =
            paymentTermsType != null ? paymentTermsType.getPaymentTermsTypeDisplayName() : null;
    }
}
