package uk.gov.hmcts.opal.dto.legacy;

import jakarta.xml.bind.annotation.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class GetDefendantAccountPartyLegacyResponse {

    @XmlElement(name = "version")
    private Long version; // carried for the future ETag ticket

    @XmlElement(name = "defendant_account_party")
    private DefendantAccountPartyLegacy defendantAccountParty;
}
