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
public class OffenceReferenceLegacy {

    @JsonProperty("offence_id")
    @XmlElement(name = "offence_id")
    private Long offenceId;

    @JsonProperty("cjs_code")
    @XmlElement(name = "cjs_code")
    private String cjsCode;

    @JsonProperty("offence_title")
    @XmlElement(name = "offence_title")
    private String offenceTitle;
}
