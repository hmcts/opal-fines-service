package uk.gov.hmcts.opal.entity.projection;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CourtReferenceData(
    @JsonProperty("court_id") Long courtId,
    @JsonProperty("business_unit_id") Short businessUnitId,
    @JsonProperty("court_code") Short courtCode,
    @JsonProperty("name") String name,
    @JsonProperty("name_cy") String nameCy,
    @JsonProperty("national_court_code") String nationalCourtCode) {
}
