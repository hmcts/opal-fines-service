package uk.gov.hmcts.opal.dto.common;

import lombok.Getter;

/**
 * Language Preference object.
 * <p>
 * Language Code:
 *   - CY = "Welsh and English"
 *   - EN = "English only"
 * </p>
 */
@Getter
public enum LanguagePreference {

    CY("Welsh and English"),
    EN("English only");

    /**
     * -- GETTER --
     * UI-friendly display name.
     */
    // Derived from languageCode
    private final String languageDisplayName;

    LanguagePreference(String languageDisplayName) {
        this.languageDisplayName = languageDisplayName;
    }

    /** Code (CY / EN) — what’s stored in DB or JSON. */
    public String getLanguageCode() {
        return name();
    }

    /** Factory method from raw string (case-insensitive). */
    public static LanguagePreference fromCode(String languageCode) {
        if (languageCode == null) {
            return null;
        }
        return LanguagePreference.valueOf(languageCode.trim().toUpperCase());
    }

    @Override
    public String toString() {
        return name() + " (" + languageDisplayName + ")";
    }
}
