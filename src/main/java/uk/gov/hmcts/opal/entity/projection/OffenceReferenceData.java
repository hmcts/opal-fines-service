package uk.gov.hmcts.opal.entity.projection;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;

public record OffenceReferenceData(
    @JsonProperty("offence_id") Long offenceId,
    @JsonProperty("cjs_code") String cjsCode,
    @JsonProperty("business_unit_id") Short businessUnitId,
    @JsonProperty("offence_title") String offenceTitle,
    @JsonProperty("offence_title_cy") String offenceTitleCy,
    @JsonProperty("date_used_from") ZonedDateTime dateUsedFrom,
    @JsonProperty("date_used_to") ZonedDateTime dateUsedTo,
    @JsonProperty("offence_oas") String offenceOas,
    @JsonProperty("offence_oas_cy") String offenceOasCy) {
}
