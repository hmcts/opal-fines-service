package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Outbound JSON version of language preferences for legacy responses.
 * Only exposes language_code (no language_display_name).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LegacyLanguagePreferencesJson {

    @JsonProperty("document_language_preference")
    private LegacyLanguagePreferenceJson documentLanguagePreference;

    @JsonProperty("hearing_language_preference")
    private LegacyLanguagePreferenceJson hearingLanguagePreference;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LegacyLanguagePreferenceJson {
        @JsonProperty("language_code")
        private String languageCode;
    }
}
