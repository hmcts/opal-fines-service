package uk.gov.hmcts.opal.dto.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Builder;

@Builder
public class InstalmentPeriod {
    // instalment period codes
    public enum InstalmentPeriodCode {
        W("Weekly"),   // Weekly
        M("Monthly"),   // Monthly
        F("Fortnightly");   // Fortnightly

        // UI-friendly display name derived from InstalmentPeriodCode
        private final String instalmentPeriodDisplayName;

        InstalmentPeriodCode(String instalmentPeriodDisplayName) {
            this.instalmentPeriodDisplayName = instalmentPeriodDisplayName;
        }

        @JsonValue
        public String getInstalmentPeriodDisplayName() {
            return instalmentPeriodDisplayName;
        }

        // Lookup by short code string
        @JsonCreator
        public static InstalmentPeriodCode fromValue(String code) {
            if (code == null) {
                return null;
            }

            for (InstalmentPeriodCode value : values()) {
                if (value.name().equalsIgnoreCase(code.trim())) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Invalid instalment period code: " + code);
        }
    }

    // Static factory method from string
    public static InstalmentPeriod fromCode(String code) {
        return new InstalmentPeriod(InstalmentPeriodCode.fromValue(code));
    }

    @JsonIgnore
    private final InstalmentPeriodCode instalmentPeriodCode;

    // Constructor
    public InstalmentPeriod(
        @JsonProperty("instalment_period_code") InstalmentPeriodCode code) {
        this.instalmentPeriodCode = code;
    }

    // Getter for short code (W, M, F)
    @JsonProperty("instalment_period_code")
    public String getInstalmentPeriodCode() {
        return instalmentPeriodCode == null ? null : instalmentPeriodCode.toString();
    }

    @JsonProperty("instalment_period_display_name")
    public String getInstalmentPeriodDisplayName() {
        return instalmentPeriodCode == null ? null : instalmentPeriodCode.getInstalmentPeriodDisplayName();
    }

    @Override
    public String toString() {
        return getInstalmentPeriodCode() + " (" + getInstalmentPeriodDisplayName() + ")";
    }
}
