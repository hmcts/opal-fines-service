package uk.gov.hmcts.opal.dto.legacy.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.opal.dto.ToXmlString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"ljaId", "ljaName"})
public class LjaReference implements ToXmlString {

    @JsonProperty(value = "lja_id", required = true)
    @XmlElement(name = "lja_id", required = true)
    private Integer ljaId;

    @JsonProperty(value = "lja_name", required = true)
    @XmlElement(name = "lja_name", required = true)
    private String ljaName;
}