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
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.ToXmlString;
import uk.gov.hmcts.opal.dto.legacy.common.AddressDetails;
import uk.gov.hmcts.opal.dto.legacy.common.PartyDetails;
import uk.gov.hmcts.opal.dto.legacy.common.Payment;

import java.math.BigInteger;

@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
    "party",
    "address",
    "creditorAccountId",
    "creditorAccountVersion",
    "defendant",
    "payment",
    "errorResponse"
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class  LegacyGetMinorCreditorAccountAtAGlanceResponse implements ToXmlString {

    @XmlElement(name = "party", required = false)
    private PartyDetails party;

    @XmlElement(name = "address", required = false)
    private AddressDetails address;

    @XmlElement(name = "creditor_account_id", required = false)
    private Long creditorAccountId; // xsd:long

    @XmlElement(name = "creditor_account_version", required = false)
    private BigInteger creditorAccountVersion;

    @XmlElement(name = "defendant", required = false)
    private Defendant defendant;

    @XmlElement(name = "payment", required = false)
    private Payment payment;

    @XmlElement(name = "error_response", required = false)
    private ErrorResponse errorResponse;

    public GetMinorCreditorAccountAtAGlanceResponse toOpalResponse(LegacyGetMinorCreditorAccountAtAGlanceResponse legacyResponse) {

    }
}


