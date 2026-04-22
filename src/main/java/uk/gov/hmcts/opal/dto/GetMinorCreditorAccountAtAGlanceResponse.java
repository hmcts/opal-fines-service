package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.common.AddressDetails;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.util.Versioned;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetMinorCreditorAccountAtAGlanceResponse implements ToJsonString, Versioned {

    @JsonIgnore
    private BigInteger version;

    @JsonProperty("party")
    private PartyDetails party;

    @JsonProperty("address")
    private AddressDetails address;

    @JsonProperty("creditor_account_id")
    private Long creditorAccountId;

    @JsonProperty("defendant")
    private AtAGlanceDefendant defendant;

    @JsonProperty("payment")
    private Payment payment;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AtAGlanceDefendant {

        @JsonProperty("account_number")
        private String accountNumber;

        @JsonProperty("account_id")
        private Long accountId;

        @JsonProperty("title")
        private String title;

        @JsonProperty("forenames")
        private String forenames;

        @JsonProperty("surname")
        private String surname;
    }
}
