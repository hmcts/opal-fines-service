package uk.gov.hmcts.opal.dto.reference;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CourtReferenceData(
    @JsonProperty("court_id") Long courtId,
    @JsonProperty("business_unit_id") Short businessUnitId,
    @JsonProperty("court_code") Short courtCode,
    @JsonProperty("name") String name,
    @JsonProperty("court_type") String courtType,
    @JsonProperty("lja") Short localJusticeAreaId,
    @JsonProperty("division") String division) {
}
