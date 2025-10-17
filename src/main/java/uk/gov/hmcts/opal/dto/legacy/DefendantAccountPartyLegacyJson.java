package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.opal.dto.common.DefendantAccountParty;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DefendantAccountPartyLegacyJson extends DefendantAccountParty {
    @JsonProperty("defendant_account_party_id")
    private String defendantAccountPartyId;

    @JsonProperty("language_preferences")
    private LegacyLanguagePreferencesJson legacyLanguagePreferences;
}
