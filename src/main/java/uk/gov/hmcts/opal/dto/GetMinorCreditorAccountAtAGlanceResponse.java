package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.common.AddressDetails;
import uk.gov.hmcts.opal.dto.common.PartyDetails;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetMinorCreditorAccountAtAGlanceResponse implements ToJsonString {

    @JsonProperty("party")
    private PartyDetails party;

    @JsonProperty("address")
    private AddressDetails address;

    @JsonProperty("creditor_account_id")
    private Long creditorAccountId;

    @JsonProperty("defendant")
    private DefendantDto defendant;

    @JsonProperty("payment")
    private Payment payment;
}
