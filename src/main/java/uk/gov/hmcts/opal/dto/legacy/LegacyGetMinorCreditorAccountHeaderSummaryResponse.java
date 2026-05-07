package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.dto.legacy.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.legacy.common.CreditorAccountTypeReference;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LegacyGetMinorCreditorAccountHeaderSummaryResponse implements ToJsonString {

    @JsonProperty("party_details")
    private PartyDetailsLegacy partyDetails;

    @JsonProperty("business_unit")
    private BusinessUnitSummary businessUnit;

    @JsonProperty("creditor")
    private CreditorHeaderLegacy creditor;

    @JsonProperty("financials")
    private FinancialsLegacy financials;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CreditorHeaderLegacy {

        @JsonProperty("account_version")
        private Integer accountVersion;

        @JsonProperty("account_id")
        private String accountId;

        @JsonProperty("account_number")
        private String accountNumber;

        @JsonProperty("account_type")
        private CreditorAccountTypeReference accountType;

        @JsonProperty("has_associated_defendant")
        private Boolean hasAssociatedDefendant;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FinancialsLegacy {

        @JsonProperty("awarded")
        private BigDecimal awarded;

        @JsonProperty("paid_out")
        private BigDecimal paidOut;

        @JsonProperty("awaiting_payout")
        private BigDecimal awaitingPayout;

        @JsonProperty("outstanding")
        private BigDecimal outstanding;
    }
}
