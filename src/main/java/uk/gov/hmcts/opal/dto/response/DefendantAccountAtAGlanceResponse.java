package uk.gov.hmcts.opal.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DefendantAccountAtAGlanceResponse implements ToJsonString {

    //FIXME - based on getDefendantAccountAtAGlanceResponse.json

    @JsonProperty("defendant_account_id")
    private String defendantAccountId;

    @JsonProperty("version")
    private Long version;

    @JsonProperty("account_number")
    private String accountNumber;

    // other fields
}
