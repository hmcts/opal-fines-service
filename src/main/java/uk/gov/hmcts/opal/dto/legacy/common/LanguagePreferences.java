package uk.gov.hmcts.opal.dto.legacy.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class LanguagePreferences {

    @JsonProperty("document_language_preference")
    private DocumentLanguagePreference documentLanguagePreference;

    @JsonProperty("hearing_language_preference")
    private HearingLanguagePreference hearingLanguagePreference;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Jacksonized
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class DocumentLanguagePreference {

        @JsonProperty("document_language_code")
        private String documentLanguageCode;

        @JsonProperty("document_language_display_name")
        private String documentLanguageDisplayName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Jacksonized
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class HearingLanguagePreference {

        @JsonProperty("hearing_language_code")
        private String hearingLanguageCode;

        @JsonProperty("hearing_language_display_name")
        private String hearingLanguageDisplayName;
    }
}
