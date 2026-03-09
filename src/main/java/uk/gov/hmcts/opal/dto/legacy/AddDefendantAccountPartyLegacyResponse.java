package uk.gov.hmcts.opal.dto.legacy;

import jakarta.xml.bind.annotation.*;
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
@XmlType(propOrder = {"version", "defendantAccountParty"})
public class AddDefendantAccountPartyLegacyResponse {

    @XmlElement(name = "version")
    private Integer version;

    @XmlElement(name = "defendant_account_party")
    private DefendantAccountPartyLegacy defendantAccountParty;

}
