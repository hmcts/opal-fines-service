package uk.gov.hmcts.opal.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.util.Versioned;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
public class RemoveDefendantAccountPartyResponse implements ToJsonString, Versioned {

    @JsonProperty("defendant_account_party_id")
    private String defendantAccountPartyId;

    @JsonIgnore
    private BigInteger version;
}
