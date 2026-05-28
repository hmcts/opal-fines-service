package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyCreditorAccountPaymentDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyUpdateMinorCreditorAccountRequest {

    @JsonProperty("creditor_account_id")
    @XmlElement(name = "creditor_account_id")
    @NotNull
    private String creditorAccountId;

    @JsonProperty("business_unit_id")
    @XmlElement(name = "business_unit_id")
    @NotNull
    @Size(min = 1)
    private String businessUnitId;

    @JsonProperty("business_unit_user_id")
    @XmlElement(name = "business_unit_user_id")
    @NotNull
    @Size(min = 1)
    private String businessUnitUserId;

    @JsonProperty("account_version")
    @XmlElement(name = "account_version")
    @NotNull
    private Integer accountVersion;

    @JsonProperty("party_details")
    @XmlElement(name = "party_details")
    @NotNull
    private LegacyPartyDetails partyDetails;

    @JsonProperty("address")
    @XmlElement(name = "address")
    @NotNull
    private AddressDetailsLegacy address;

    @JsonProperty("payment")
    @XmlElement(name = "payment")
    @NotNull
    private LegacyCreditorAccountPaymentDetails payment;
}
