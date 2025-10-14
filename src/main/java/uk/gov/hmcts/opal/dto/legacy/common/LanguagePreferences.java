package uk.gov.hmcts.opal.dto.legacy.common;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.opal.dto.ToXmlString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class LanguagePreferences implements ToXmlString {

    @XmlElement(name = "document_language_preference")
    private DocumentLanguagePreference documentLanguagePreference;

    @XmlElement(name = "hearing_language_preference")
    private HearingLanguagePreference hearingLanguagePreference;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Jacksonized
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class DocumentLanguagePreference {

        @XmlElement(name = "document_language_code")
        private String documentLanguageCode;

        @XmlElement(name = "document_language_display_name")
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

        @XmlElement(name = "hearing_language_code")
        private String hearingLanguageCode;

        @XmlElement(name = "hearing_language_display_name")
        private String hearingLanguageDisplayName;
    }
}
