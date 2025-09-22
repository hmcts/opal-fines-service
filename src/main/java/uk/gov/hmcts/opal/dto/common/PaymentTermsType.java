package uk.gov.hmcts.opal.dto.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentTermsType {

    // Payment terms type codes
    public enum PaymentTermsTypeCode {
        B("By date"),
        P("Paid"),
        I("Instalments");

        // UI-friendly display name derived from PaymentTermsTypeCode
        private final String paymentTermsTypeDisplayName;

        PaymentTermsTypeCode(String paymentTermsTypeDisplayName) {
            this.paymentTermsTypeDisplayName = paymentTermsTypeDisplayName;
        }

        public String getPaymentTermsTypeDisplayName() {
            return paymentTermsTypeDisplayName;
        }

        // Lookup by short code string
        @JsonCreator
        public static PaymentTermsTypeCode fromValue(String code) {
            if (code == null) {
                return null;
            }

            for (PaymentTermsTypeCode value : values()) {
                if (value.name().equalsIgnoreCase(code.trim())) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Invalid Payment Terms Type code: " + code);
        }
    }

    // Static factory method from string
    public static PaymentTermsType fromCode(String code) {
        return new PaymentTermsType(PaymentTermsTypeCode.fromValue(code));
    }

    @JsonIgnore
    private final PaymentTermsTypeCode paymentTermsTypeCode;

    // Constructor
    public PaymentTermsType(
        @JsonProperty("payment_terms_type_code") PaymentTermsTypeCode code) {
        this.paymentTermsTypeCode = code;
    }

    // Getter for short code (B, P, I)
    @JsonProperty("payment_terms_type_code")
    public String getPaymentTermsTypeCode() {
        return paymentTermsTypeCode == null ? null : paymentTermsTypeCode.toString();
    }

    @JsonProperty("payment_terms_type_display_name")
    public String getPaymentTermsTypeDisplayName() {
        return paymentTermsTypeCode == null ? null : paymentTermsTypeCode.getPaymentTermsTypeDisplayName();
    }

    @Override
    public String toString() {
        return getPaymentTermsTypeCode() + " (" + getPaymentTermsTypeDisplayName() + ")";
    }
}
