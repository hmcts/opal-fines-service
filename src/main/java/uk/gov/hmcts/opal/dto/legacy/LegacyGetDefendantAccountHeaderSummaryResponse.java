package uk.gov.hmcts.opal.dto.legacy;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.opal.dto.ToXmlString;
import uk.gov.hmcts.opal.dto.legacy.common.AccountStatusReference;
import uk.gov.hmcts.opal.dto.legacy.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.legacy.common.DefendantDetails;
import uk.gov.hmcts.opal.dto.legacy.common.PaymentStateSummary;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyGetDefendantAccountHeaderSummaryResponse implements ToXmlString {

    @JsonProperty("version")
    private Long version;

    @JsonProperty("defendant_account_id")
    private String defendantAccountId;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("defendant_party_id")
    private String defendantPartyId;

    @JsonProperty("parent_guardian_party_id")
    private String parentGuardianPartyId;

    @JsonProperty("account_status_reference")
    private AccountStatusReference accountStatusReference;

    @JsonProperty("account_type")
    private String accountType;

    @JsonProperty("prosecutor_case_reference")
    private String prosecutorCaseReference;

    @JsonProperty("fixed_penalty_ticket_number")
    private String fixedPenaltyTicketNumber;

    @JsonProperty("business_unit_summary")
    private BusinessUnitSummary businessUnitSummary;

    @JsonProperty("payment_state_summary")
    private PaymentStateSummary paymentStateSummary;

    @JsonProperty("defendant_details")
    private DefendantDetails defendantDetails;

}
