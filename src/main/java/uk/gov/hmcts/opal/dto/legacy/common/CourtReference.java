package uk.gov.hmcts.opal.dto.legacy.common;


import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToXmlString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CourtReference implements ToXmlString {

    @XmlElement(name = "court_id")
    private Long courtId;

    // Court code is not returned from Legacy, and needs to be populated from the Opal DB.
    private Short courtCode;

    @XmlElement(name = "court_name")
    private String courtName;
}
