package uk.gov.hmcts.opal.dto.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder
public class InstalmentPeriod {
    // instalment period codes
    @Getter
    public enum InstalmentPeriodCode {
        W("Weekly"),   // Weekly
        M("Monthly"),   // Monthly
        F("Fortnightly");   // Fortnightly

        // UI-friendly display name derived from InstalmentPeriodCode
        private final String instalmentPeriodDisplayName;

        InstalmentPeriodCode(String instalmentPeriodDisplayName) {
            this.instalmentPeriodDisplayName = instalmentPeriodDisplayName;
        }

        // Lookup by short code string
        @JsonCreator
        public static InstalmentPeriodCode fromValue(String code) {
            if (code == null) {
                return null;
            }
            return java.util.Arrays.stream(values())
                .filter(value -> value.name().equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid instalment period code: " + code));
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
    public InstalmentPeriodCode getInstalmentPeriodCode() {
        return instalmentPeriodCode;
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
