package uk.gov.hmcts.opal.entity.projection;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OffenceSearchData(
    @JsonProperty("offence_id") Long offenceId,
    @JsonProperty("cjs_code") String cjsCode,
    @JsonProperty("offence_title") String offenceTitle,
    @JsonProperty("offence_title_cy") String offenceTitleCy,
    @JsonProperty("date_used_from") LocalDateTime dateUsedFrom,
    @JsonProperty("date_used_to") LocalDateTime dateUsedTo,
    @JsonProperty("offence_oas") String offenceOas,
    @JsonProperty("offence_oas_cy") String offenceOasCy) {
}
