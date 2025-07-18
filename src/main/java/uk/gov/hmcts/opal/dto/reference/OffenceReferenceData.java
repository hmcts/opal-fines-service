package uk.gov.hmcts.opal.dto.reference;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OffenceReferenceData(
    @JsonProperty("offence_id") Long offenceId,
    @JsonProperty("cjs_code") String cjsCode,
    @JsonProperty("business_unit_id") Short businessUnitId,
    @JsonProperty("offence_title") String offenceTitle,
    @JsonProperty("offence_title_cy") String offenceTitleCy,
    @JsonProperty("date_used_from") OffsetDateTime dateUsedFrom,
    @JsonProperty("date_used_to") OffsetDateTime dateUsedTo,
    @JsonProperty("offence_oas") String offenceOas,
    @JsonProperty("offence_oas_cy") String offenceOasCy) {
}
