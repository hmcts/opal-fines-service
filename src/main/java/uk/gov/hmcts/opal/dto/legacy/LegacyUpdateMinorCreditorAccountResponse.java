package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.common.legacy.model.ErrorResponse;
import uk.gov.hmcts.opal.dto.ToXmlString;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyCreditorAccountPaymentDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "response")
public class LegacyUpdateMinorCreditorAccountResponse implements ToXmlString {

    @XmlElement(name = "account_version")
    @NotNull
    private Integer accountVersion;

    @XmlElement(name = "creditor_account_id")
    @NotNull
    private Long creditorAccountId;

    @XmlElement(name = "party_details")
    @NotNull
    private LegacyPartyDetails partyDetails;

    @XmlElement(name = "address")
    @NotNull
    private AddressDetailsLegacy address;

    @XmlElement(name = "payment")
    @NotNull
    private LegacyCreditorAccountPaymentDetails payment;

    @XmlElement(name = "error_response")
    private ErrorResponse errorResponse;
}
