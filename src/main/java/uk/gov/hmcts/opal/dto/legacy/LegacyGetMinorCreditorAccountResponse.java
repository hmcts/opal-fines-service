package uk.gov.hmcts.opal.dto.legacy;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.common.legacy.model.ErrorResponse;
import uk.gov.hmcts.opal.common.legacy.model.HasErrorResponse;
import uk.gov.hmcts.opal.dto.ToXmlString;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyCreditorAccountPaymentDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails;

@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
    "accountVersion",
    "creditorAccountId",
    "partyDetails",
    "address",
    "payment",
    "errorResponse"
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LegacyGetMinorCreditorAccountResponse implements ToXmlString, HasErrorResponse {

    @XmlElement(name = "account_version", required = true)
    private Long accountVersion;

    @XmlElement(name = "creditor_account_id", required = true)
    private Long creditorAccountId;

    @XmlElement(name = "party_details", required = true)
    private LegacyPartyDetails partyDetails;

    @XmlElement(name = "address", required = true)
    private AddressDetailsLegacy address;

    @XmlElement(name = "payment", required = true)
    private LegacyCreditorAccountPaymentDetails payment;

    @XmlElement(name = "error_response")
    private ErrorResponse errorResponse;
}
