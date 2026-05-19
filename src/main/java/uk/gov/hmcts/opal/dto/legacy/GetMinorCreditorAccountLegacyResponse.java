package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@JsonIgnoreProperties(ignoreUnknown = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class GetMinorCreditorAccountLegacyResponse implements ToXmlString {

    @JsonProperty("account_version")
    @XmlElement(name = "account_version")
    private Integer accountVersion;

    @JsonProperty("creditor_account_id")
    @XmlElement(name = "creditor_account_id")
    private Long creditorAccountId;

    @JsonProperty("party_details")
    @XmlElement(name = "party_details")
    private PartyDetailsLegacy partyDetails;

    @JsonProperty("address")
    @XmlElement(name = "address")
    private AddressDetailsLegacy address;

    @JsonProperty("payment")
    @XmlElement(name = "payment")
    private CreditorAccountPaymentDetailsLegacy payment;
}
