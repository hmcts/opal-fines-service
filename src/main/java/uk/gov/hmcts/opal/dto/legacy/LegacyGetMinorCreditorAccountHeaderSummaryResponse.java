package uk.gov.hmcts.opal.dto.legacy;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.opal.dto.ToXmlString;
import uk.gov.hmcts.opal.dto.legacy.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.legacy.common.CreditorAccountTypeReference;

import java.math.BigDecimal;

@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LegacyGetMinorCreditorAccountHeaderSummaryResponse implements ToXmlString {

    @XmlElement(name = "party_details")
    private PartyDetailsLegacy partyDetails;

    @XmlElement(name = "business_unit")
    private BusinessUnitSummary businessUnit;

    @XmlElement(name = "creditor")
    private CreditorHeaderLegacy creditor;

    @XmlElement(name = "financials")
    private FinancialsLegacy financials;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Jacksonized
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CreditorHeaderLegacy {

        @XmlElement(name = "account_version")
        private Integer accountVersion;

        @XmlElement(name = "account_id")
        private String accountId;

        @XmlElement(name = "account_number")
        private String accountNumber;

        @XmlElement(name = "account_type")
        private CreditorAccountTypeReference accountType;

        @XmlElement(name = "has_associated_defendant")
        private Boolean hasAssociatedDefendant;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Jacksonized
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class FinancialsLegacy {

        @XmlElement(name = "awarded")
        private BigDecimal awarded;

        @XmlElement(name = "paid_out")
        private BigDecimal paidOut;

        @XmlElement(name = "awaiting_payout")
        private BigDecimal awaitingPayout;

        @XmlElement(name = "outstanding")
        private BigDecimal outstanding;
    }
}
