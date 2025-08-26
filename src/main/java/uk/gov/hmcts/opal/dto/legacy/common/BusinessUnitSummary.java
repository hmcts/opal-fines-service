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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class BusinessUnitSummary {

    @XmlElement(name = "business_unit_name")
    private String businessUnitName;

    @XmlElement(name = "business_unit_id")
    private String businessUnitId;

    @XmlElement(name = "business_unit_code")
    private String businessUnitCode;

    @XmlElement(name = "welsh_speaking")
    private String welshSpeaking;

}
