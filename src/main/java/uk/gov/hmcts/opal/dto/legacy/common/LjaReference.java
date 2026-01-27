package uk.gov.hmcts.opal.dto.legacy.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlElement;

import uk.gov.hmcts.opal.dto.ToXmlString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@XmlAccessorType(XmlAccessType.FIELD)
public class LjaReference implements ToXmlString {

    @XmlElement(name = "lja_id", required = true)
    private Short ljaId;

    // Local Justice Area code is not returned from Legacy, and needs to be populated from the Opal DB.
    private String ljaCode;

    @XmlElement(name = "lja_name", required = true)
    private String ljaName;
}
