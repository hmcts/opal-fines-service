package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.common.AccountStatusReference;
import uk.gov.hmcts.opal.dto.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.common.PaymentStateSummary;
import uk.gov.hmcts.opal.util.Versioned;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DefendantAccountHeaderSummary implements ToJsonString, Versioned {

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonIgnore
    private Long version;

    @JsonProperty("defendant_account_party_id")
    private String defendantAccountPartyId;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonProperty("parent_guardian_party_id")
    private String parentGuardianPartyId;

    @JsonProperty("account_type")
    private String accountType;

    @JsonProperty("prosecutor_case_reference")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String prosecutorCaseReference;

    @JsonProperty("fixed_penalty_ticket_number")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String fixedPenaltyTicketNumber;

    @JsonProperty("account_status_reference")
    private AccountStatusReference accountStatusReference;

    @JsonProperty("business_unit_summary")
    private BusinessUnitSummary businessUnitSummary;

    @JsonProperty("payment_state_summary")
    private PaymentStateSummary paymentStateSummary;

    @JsonProperty("party_details")
    private PartyDetails partyDetails;
}
