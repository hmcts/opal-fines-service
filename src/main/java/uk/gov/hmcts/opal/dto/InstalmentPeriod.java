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
public class InstalmentPeriod {

    @JsonProperty("instalment_period_code")
    private InstalmentPeriodCode instalmentPeriodCode;

    public enum InstalmentPeriodCode {
        W("W"),
        M("M"),
        F("F");

        private final String value;

        InstalmentPeriodCode(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @JsonCreator
        public static InstalmentPeriodCode fromValue(String value) {
            for (InstalmentPeriodCode code : values()) {
                if (code.value.equals(value)) {
                    return code;
                }
            }
            throw new IllegalArgumentException("Unknown value: " + value);
        }
    }
}
