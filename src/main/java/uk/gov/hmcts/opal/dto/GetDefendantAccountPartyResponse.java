package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.common.DefendantAccountParty;
import uk.gov.hmcts.opal.util.Versioned;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetDefendantAccountPartyResponse implements Versioned {

    @JsonIgnore
    private Long version;

    @JsonProperty("defendant_account_party")
    private DefendantAccountParty defendantAccountParty;

}
