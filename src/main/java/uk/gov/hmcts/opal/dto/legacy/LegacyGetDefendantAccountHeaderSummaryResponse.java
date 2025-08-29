package uk.gov.hmcts.opal.dto.legacy;


import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToXmlString;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails;
import uk.gov.hmcts.opal.dto.legacy.common.AccountStatusReference;
import uk.gov.hmcts.opal.dto.legacy.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.legacy.common.PaymentStateSummary;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyGetDefendantAccountHeaderSummaryResponse implements ToXmlString {

    @XmlElement(name = "version")
    private Long version;

    @XmlElement(name = "defendant_account_id")
    private String defendantAccountId;

    @XmlElement(name = "account_number")
    private String accountNumber;

    @XmlElement(name = "defendant_party_id")
    private String defendantPartyId;

    @XmlElement(name = "parent_guardian_party_id")
    private String parentGuardianPartyId;

    @XmlElement(name = "account_status_reference")
    private AccountStatusReference accountStatusReference;

    @XmlElement(name = "account_type")
    private String accountType;

    @XmlElement(name = "prosecutor_case_reference")
    private String prosecutorCaseReference;

    @XmlElement(name = "fixed_penalty_ticket_number")
    private String fixedPenaltyTicketNumber;

    @XmlElement(name = "business_unit_summary")
    private BusinessUnitSummary businessUnitSummary;

    @XmlElement(name = "payment_state_summary")
    private PaymentStateSummary paymentStateSummary;

    @XmlElement(name = "party_details")
    private LegacyPartyDetails partyDetails;

}
