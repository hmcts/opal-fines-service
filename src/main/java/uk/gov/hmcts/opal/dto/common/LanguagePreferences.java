package uk.gov.hmcts.opal.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LanguagePreferences {

    @JsonProperty("document_language_preference")
    private LanguagePreference documentLanguagePreference;

    @JsonProperty("hearing_language_preference")
    private LanguagePreference hearingLanguagePreference;

    public static LanguagePreferences ofCodes(String documentCode, String hearingCode) {
        return new LanguagePreferences(
            LanguagePreference.fromCode(documentCode),
            LanguagePreference.fromCode(hearingCode)
        );
    }
}
