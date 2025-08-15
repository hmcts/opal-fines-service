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
public class LanguagePreferenceDTO {

    @JsonProperty("language_code")
    private String languageCode;

    @JsonProperty("language_display_name")
    private String languageDisplayName; // always derived from languageCode

    public LanguagePreferenceDTO(LanguagePreference pref) {
        if (pref != null) {
            this.languageCode = pref.getLanguageCode();
            this.languageDisplayName = pref.getLanguageDisplayName();
        }
    }

    public static LanguagePreferenceDTO ofCode(String code) {
        LanguagePreference pref = LanguagePreference.fromCode(code);
        return new LanguagePreferenceDTO(pref);
    }

    /** Setting the code automatically derives the display name. */
    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
        LanguagePreference pref = LanguagePreference.fromCode(languageCode);
        this.languageDisplayName = pref != null ? pref.getLanguageDisplayName() : null;
    }
}
