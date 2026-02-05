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
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.opal.dto.ToXmlString;

import java.math.BigInteger;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPayment;

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
    private AddressDetailsLegacy address;

    @XmlElement(name = "creditor_account_id", required = true)
    private Long creditorAccountId; // xsd:long

    @XmlElement(name = "creditor_account_version", required = true)
    private BigInteger creditorAccountVersion;

    @XmlElement(name = "defendant")
    private AtAGlanceDefendant defendant;

    @XmlElement(name = "payment")
    private LegacyPayment payment;

    @XmlElement(name = "error_response")
    private ErrorResponse errorResponse;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Jacksonized
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class AtAGlanceDefendant {

        @XmlElement(name = "account_number")
        private String accountNumber;

        @XmlElement(name = "account_id")
        private Long accountId;
    }
}