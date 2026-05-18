package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlRootElement(name = "request")
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyUpdateMinorCreditorAccountRequest {

    @JsonProperty("creditor_account_id")
    @XmlElement(name = "creditor_account_id")
    private String creditorAccountId;

    @JsonProperty("account_version")
    @XmlElement(name = "account_version")
    private Integer accountVersion;

    @JsonProperty("business_unit_id")
    @XmlElement(name = "business_unit_id")
    private String businessUnitId;

    @JsonProperty("business_unit_user_id")
    @XmlElement(name = "business_unit_user_id")
    private String businessUnitUserId;

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
