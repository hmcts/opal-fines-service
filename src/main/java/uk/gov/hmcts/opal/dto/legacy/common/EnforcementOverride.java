package uk.gov.hmcts.opal.dto.legacy.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToXmlString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@XmlAccessorType(XmlAccessType.FIELD)
public class EnforcementOverride implements ToXmlString {

    private String overrideReason;

    @JsonProperty("enforcement_override_result")
    @XmlElement(name = "enforcement_override_result")
    @NotNull
    private EnforcementOverrideResultReference enforcementOverrideResult;

    @JsonProperty("enforcer")
    @XmlElement(name = "enforcer")
    private EnforcerReference enforcer;

    @JsonProperty("lja")
    @XmlElement(name = "lja")
    private LjaReference lja;
}
