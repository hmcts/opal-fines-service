package uk.gov.hmcts.opal.dto.legacy;

import jakarta.validation.constraints.NotBlank;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "response")
public class AddDefendantAccountEnforcementLegacyResponse {

    @XmlElement(name = "defendant_account_id")
    @NotBlank
    private String defendantAccountId;

    @XmlElement(name = "version")
    private Integer version;

    @XmlElement(name = "enforcement_id")
    private String enforcementId;

}
