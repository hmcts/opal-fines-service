package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.util.Versioned;

/**
 * Response returned after updating a minor creditor account.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
public class MinorCreditorAccountResponse implements ToJsonString, Versioned {

    @JsonProperty("creditor_account_id")
    private Long creditorAccountId;

    @JsonProperty("payout_hold")
    private PayoutHold payoutHold;

    @JsonIgnore
    private BigInteger version;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public static class PayoutHold {
        @JsonProperty("payout_hold")
        private Boolean payoutHold;
    }
}
