package uk.gov.hmcts.opal.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentTermsType {

    B("By date"),
    P("Paid"),
    I("Instalments");

    /**
     * -- GETTER --
     * UI-friendly display name.
     */
    // Derived from paymentTermsTypeCode
    private final String paymentTermsTypeDisplayName;

    /** Code (B / P / I) — what’s stored in DB or JSON. */
    public String getPaymentTermsTypeCode() {
        return name();
    }

    /** Factory method from raw string (case-insensitive). */
    public static PaymentTermsType fromCode(String paymentTermsTypeCode) {
        if (paymentTermsTypeCode == null) {
            return null;
        }
        return PaymentTermsType.valueOf(paymentTermsTypeCode.trim().toUpperCase());
    }

    @Override
    public String toString() {
        return name() + " (" + paymentTermsTypeDisplayName + ")";
    }
}
