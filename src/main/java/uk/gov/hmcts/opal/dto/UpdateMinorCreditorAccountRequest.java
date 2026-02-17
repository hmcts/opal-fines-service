package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for PATCH /minor-creditor-accounts/{id} (Opal mode).
 * Exactly one update group must be present. No IDs in the body.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateMinorCreditorAccountRequest implements ToJsonString {

    @JsonProperty("payout_hold")
    private PayoutHold payoutHold;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PayoutHold {
        @JsonProperty("payout_hold")
        private Boolean payoutHold;
    }
}
