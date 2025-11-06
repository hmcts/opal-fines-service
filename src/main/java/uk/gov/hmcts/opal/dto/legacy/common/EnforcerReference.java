package uk.gov.hmcts.opal.dto.legacy.common;

import java.io.Serializable;
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
@XmlType(propOrder = {"enforcerId", "enforcerName"})
public class EnforcerReference implements ToXmlString {

    @JsonProperty(value = "enforcer_id", required = true)
    @XmlElement(name = "enforcer_id", required = true)
    private Integer enforcerId;

    @JsonProperty(value = "enforcer_name", required = true)
    @XmlElement(name = "enforcer_name", required = true)
    private String enforcerName;
}