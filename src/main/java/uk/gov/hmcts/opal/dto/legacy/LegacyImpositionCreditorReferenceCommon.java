package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.generated.model.ImpositionCreditorReferenceCommon.AccountTypeEnum;
import uk.gov.hmcts.opal.generated.model.ImpositionCreditorReferenceCommon.DisplayNameEnum;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyImpositionCreditorReferenceCommon {

    @JsonProperty("creditor_account_id")
    @XmlElement(name = "creditor_account_id")
    private Long creditorAccountId;

    @JsonProperty("account_type")
    @XmlElement(name = "account_type")
    private AccountTypeEnum accountType;

    @JsonProperty("display_name")
    @XmlElement(name = "display_name")
    private DisplayNameEnum displayName;

    @JsonProperty("major_creditor_id")
    @XmlElement(name = "major_creditor_id")
    private Long majorCreditorId;

    @JsonProperty("minor_creditor_party_id")
    @XmlElement(name = "minor_creditor_party_id")
    private Long minorCreditorPartyId;

    @JsonProperty("name")
    @XmlElement(name = "name")
    private String name;
}
