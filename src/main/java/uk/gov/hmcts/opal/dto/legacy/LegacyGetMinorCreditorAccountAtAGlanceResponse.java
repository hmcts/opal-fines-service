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

    @XmlElement(name = "party", required = true)
    private LegacyPartyDetails party;

    @XmlElement(name = "address", required = true)
    private LegacyAddressDetails address;

    @XmlElement(name = "creditor_account_id", required = true)
    private Long creditorAccountId; // xsd:long

    @XmlElement(name = "creditor_account_version", required = true)
    private BigInteger creditorAccountVersion;

    @XmlElement(name = "defendant")
    private LegacyDefendant defendant;

    @XmlElement(name = "payment")
    private LegacyPayment payment;

    @XmlElement(name = "error_response")
    private ErrorResponse errorResponse;

    public GetMinorCreditorAccountAtAGlanceResponse toOpalResponse() {
        return GetMinorCreditorAccountAtAGlanceResponse.builder()
            .party(this.getParty() == null ? null : this.getParty().toOpalDto())
            .address(this.getAddress() == null ? null : this.getAddress().toOpalDto())
            .creditorAccountId(this.getCreditorAccountId() == null ? null : this.getCreditorAccountId())
            .defendant(this.getDefendant() == null ? null : this.getDefendant().toOpalDto())
            .payment(this.getPayment() == null ? null : this.getPayment().toOpalDto())
            .build();
    }
}


