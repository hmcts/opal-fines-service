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
public class LegacyCourtReferenceCommon {

    @JsonProperty("court_id")
    @XmlElement(name = "court_id")
    private Long courtId;

    @JsonProperty("court_code")
    @XmlElement(name = "court_code")
    private Integer courtCode;

    @JsonProperty("court_name")
    @XmlElement(name = "court_name")
    private String courtName;
}
