package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.common.DefendantAccountParty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetDefendantAccountPartyLegacyResponseJson extends GetDefendantAccountPartyResponse {

    @JsonProperty("version")
    private Long version;

    public static GetDefendantAccountPartyLegacyResponseJson of(Long version, DefendantAccountParty party) {
        GetDefendantAccountPartyLegacyResponseJson r = new GetDefendantAccountPartyLegacyResponseJson();
        r.setVersion(version);
        r.setDefendantAccountParty(party);
        return r;
    }
}
