package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.util.Versioned;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetMinorCreditorAccountHeaderSummaryResponse implements ToJsonString, Versioned {

    @JsonProperty("creditor_account_id")
    private String creditorAccountId;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("creditor_account_type")
    private String creditorAccountType;

    @JsonIgnore
    private BigInteger version;

    @JsonProperty("business_unit_summary")
    private BusinessUnitSummary businessUnitSummary;

    @JsonProperty("party_details")
    private PartyDetails partyDetails;

    @JsonProperty("awarded_amount")
    private BigDecimal awardedAmount;

    @JsonProperty("paid_out_amount")
    private BigDecimal paidOutAmount;

    @JsonProperty("awaiting_payout_amount")
    private BigDecimal awaitingPayoutAmount;

    @JsonProperty("outstanding_amount")
    private BigDecimal outstandingAmount;

    @JsonProperty("has_associated_defendant")
    private Boolean hasAssociatedDefendant;
}
