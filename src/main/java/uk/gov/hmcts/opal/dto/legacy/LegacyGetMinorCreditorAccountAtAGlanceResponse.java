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
import uk.gov.hmcts.opal.dto.legacy.common.LegacyAddressDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPayment;

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
    private LegacyPartyDetails party;

    @XmlElement(name = "address", required = false)
    private LegacyAddressDetails address;

    @XmlElement(name = "creditor_account_id", required = false)
    private Long creditorAccountId; // xsd:long

    @XmlElement(name = "creditor_account_version", required = false)
    private BigInteger creditorAccountVersion;

    @XmlElement(name = "defendant", required = false)
    private LegacyDefendant defendant;

    @XmlElement(name = "payment", required = false)
    private LegacyPayment payment;

    @XmlElement(name = "error_response", required = false)
    private ErrorResponse errorResponse;

    public GetMinorCreditorAccountAtAGlanceResponse toOpalResponse() {
        return GetMinorCreditorAccountAtAGlanceResponse.builder()
            .party(this.getParty().toOpalDto())
            .address(this.getAddress().toOpalDto())
            .creditorAccountId(this.getCreditorAccountId())
            .defendant(this.getDefendant().toOpalDto())
            .payment(this.getPayment().toOpalDto()).build();
    }
}


