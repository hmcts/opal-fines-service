package uk.gov.hmcts.opal.entity.projection;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.opal.dto.ToJsonString;

public record MajorCreditorReferenceData(
    @JsonProperty("major_creditor_id") Long majorCreditorId,
    @JsonProperty("business_unit_id") Short businessUnitId,
    @JsonProperty("major_creditor_code") String majorCreditorCode,
    @JsonProperty("name") String name,
    @JsonProperty("postcode") String postcode) implements ToJsonString {

}
