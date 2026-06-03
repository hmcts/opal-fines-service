package uk.gov.hmcts.opal.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToJsonString;

/**
 * DTO for Remove Defendant Account Party request.
 * Contains the defendant account party ID to be removed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RemoveDefendantAccountPartyRequest implements ToJsonString {

    @JsonProperty("defendant_account_party_id")
    private Long defendantAccountPartyId;

    @JsonIgnore
    private BigInteger version;
}


