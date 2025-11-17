package uk.gov.hmcts.opal.dto.legacy;

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
@XmlRootElement(name = "request")
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyReplaceDefendantAccountPartyRequest {

    @XmlElement(name = "version")
    private Long version;

    @XmlElement(name = "defendant_account_id")
    private Long defendantAccountId;

    @XmlElement(name = "business_unit_id")
    private String businessUnitId;

    @XmlElement(name = "business_unit_user_id")
    private String businessUnitUserId;

    @XmlElement(name = "defendant_account_party")
    private DefendantAccountPartyLegacy defendantAccountParty;

}
