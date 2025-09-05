package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTermsType {

    @JsonProperty("payment_terms_type_code")
    private PaymentTermsTypeCode paymentTermsTypeCode;

    public enum PaymentTermsTypeCode {
        B("B"),
        P("P"),
        I("I");

        private final String value;

        PaymentTermsTypeCode(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @JsonCreator
        public static PaymentTermsTypeCode fromValue(String value) {
            for (PaymentTermsTypeCode code : values()) {
                if (code.value.equals(value)) {
                    return code;
                }
            }
            throw new IllegalArgumentException("Unknown value: " + value);
        }
    }
}
