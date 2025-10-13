package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for the "enforcement_court" object defined in courtReference.json schema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourtReferenceDto implements ToJsonString {

    @JsonProperty("court_id")
    private Integer courtId;

    @JsonProperty("court_name")
    private String courtName;
}
