package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyOffenceReferenceCommon {

    @JsonProperty("id")
    @XmlElement(name = "id")
    private Long id;

    @JsonProperty("code")
    @XmlElement(name = "code")
    private String code;

    @JsonProperty("title")
    @XmlElement(name = "title")
    private String title;
}
