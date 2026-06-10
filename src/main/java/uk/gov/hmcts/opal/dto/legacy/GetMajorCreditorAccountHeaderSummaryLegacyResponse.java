package uk.gov.hmcts.opal.dto.legacy;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.opal.dto.ToXmlString;
import uk.gov.hmcts.opal.dto.legacy.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.legacy.common.CreditorAccountTypeReference;

@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetMajorCreditorAccountHeaderSummaryLegacyResponse implements ToXmlString {

    @XmlElement(name = "major_creditor")
    private MajorCreditorLegacy majorCreditor;

    @XmlElement(name = "business_unit_details")
    private BusinessUnitSummary businessUnitDetails;

    @XmlElement(name = "awaiting_payout")
    private BigDecimal awaitingPayout;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Jacksonized
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class MajorCreditorLegacy {

        @XmlElement(name = "creditor_account_id")
        private Long creditorAccountId;

        @XmlElement(name = "account_version")
        private Long accountVersion;

        @XmlElement(name = "account_number")
        private String accountNumber;

        @XmlElement(name = "name")
        private String name;

        @XmlElement(name = "account_reference")
        private CreditorAccountTypeReference accountReference;
    }
}
