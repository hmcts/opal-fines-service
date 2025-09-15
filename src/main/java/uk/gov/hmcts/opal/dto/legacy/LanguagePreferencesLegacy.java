package uk.gov.hmcts.opal.dto.legacy;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class LanguagePreferencesLegacy {

    @XmlElement(name = "document_language_preference")
    private LanguagePreference documentLanguagePreference;

    @XmlElement(name = "hearing_language_preference")
    private LanguagePreference hearingLanguagePreference;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class LanguagePreference {
        @XmlElement(name = "language_code")
        private String languageCode;

        @XmlElement(name = "language_display_name")
        private String languageDisplayName;
    }
}
