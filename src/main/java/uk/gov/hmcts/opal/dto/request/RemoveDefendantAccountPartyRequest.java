package uk.gov.hmcts.opal.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.common.DefendantAccountParty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)

public class RemoveDefendantAccountPartyRequest {

    @JsonProperty("defendant_account_party")
    private DefendantAccountParty defendantAccountParty;
}
