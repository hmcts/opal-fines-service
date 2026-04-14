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
import uk.gov.hmcts.opal.dto.common.CreditorAccountTypeReference;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.util.Versioned;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetMinorCreditorAccountHeaderSummaryResponse implements ToJsonString, Versioned {

    @JsonIgnore
    private BigInteger version;

    @JsonProperty("party")
    private PartyDetails party;

    @JsonProperty("business_unit")
    private BusinessUnitSummary businessUnit;

    @JsonProperty("creditor")
    private CreditorHeader creditor;

    @JsonProperty("financials")
    private Financials financials;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CreditorHeader {

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
    public static class Financials {

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
