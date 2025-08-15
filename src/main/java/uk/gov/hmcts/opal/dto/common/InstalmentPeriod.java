package uk.gov.hmcts.opal.dto.common;

import lombok.Getter;

/**
 * Instalment Period reference object.
 * <p>
 * Mapping:
 *   - W - "Weekly"
 *   - M - "Monthly"
 *   - F - "Fortnightly"
 * </p>
 */
@Getter
public enum InstalmentPeriod {

    W("Weekly"),
    M("Monthly"),
    F("Fortnightly"),;

    /**
     * -- GETTER --
     * UI-friendly display name.
     */
    // Derived from Instalment Period Code
    private final String instalmentPeriodDisplayName;

    InstalmentPeriod(String instalmentPeriodDisplayName) {
        this.instalmentPeriodDisplayName = instalmentPeriodDisplayName;
    }

    /** Code (W / M / F) — what’s stored in DB or JSON. */
    public String getInstalmentPeriodCode() {
        return name();
    }

    /** Factory method from raw string (case-insensitive). */
    public static InstalmentPeriod fromCode(String instalmentPeriodCode) {
        if (instalmentPeriodCode == null) {
            return null;
        }
        return InstalmentPeriod.valueOf(instalmentPeriodCode.trim().toUpperCase());
    }

    @Override
    public String toString() {
        return name() + " (" + instalmentPeriodDisplayName + ")";
    }
}
