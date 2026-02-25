package uk.gov.hmcts.opal.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IndividualAlias {

    @JsonProperty("alias_id")
    private String aliasId;

    @JsonProperty("sequence_number")
    private Integer sequenceNumber;

    @JsonProperty("surname")
    private String surname;

    @JsonProperty("forenames")
    private String forenames;
}
