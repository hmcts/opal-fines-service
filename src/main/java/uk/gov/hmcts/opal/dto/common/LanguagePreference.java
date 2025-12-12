package uk.gov.hmcts.opal.dto.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

/**
 * Language Preference object.
 *
 * <p>
 * Language Code:
 *   - CY = "Welsh and English"
 *   - EN = "English only"
 * </p>
 */
@Getter
@Builder
public class LanguagePreference {

    @Getter
    public enum LanguageCode {
        CY("Welsh and English"),
        EN("English only");

        // UI-friendly display name derived from languageCode
        private final String languageDisplayName;

        LanguageCode(String languageDisplayName) {
            this.languageDisplayName = languageDisplayName;
        }

        // Lookup by short code string
        @JsonCreator
        public static LanguageCode fromValue(String code) {
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
    public static LanguagePreference fromCode(String code) {
        return new LanguagePreference(LanguageCode.fromValue(code));
    }

    @JsonIgnore
    private final LanguageCode languageCode;

    // Constructor
    public LanguagePreference(
        @JsonProperty("language_code") LanguageCode code) {
        this.languageCode = code;
    }

    // Code (CY / EN) — what’s stored in DB or JSON.
    @JsonProperty("language_code")
    public String getLanguageCode() {
        return languageCode == null ? null : languageCode.toString();
    }

    @JsonProperty("language_display_name")
    public String getLanguageDisplayName() {
        return languageCode == null ? null : languageCode.getLanguageDisplayName();
    }

    @Override
    public String toString() {
        return getLanguageCode() + " (" + getLanguageDisplayName() + ")";
    }
}
