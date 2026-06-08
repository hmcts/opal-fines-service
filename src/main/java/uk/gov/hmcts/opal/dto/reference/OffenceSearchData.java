package uk.gov.hmcts.opal.dto.reference;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OffenceSearchData {
    @JsonProperty("offence_id") Long offenceId;
    @JsonProperty("cjs_code") String cjsCode;
    @JsonProperty("offence_title") String offenceTitle;
    @JsonProperty("offence_title_cy") String offenceTitleCy;
    @JsonProperty("date_used_from") OffsetDateTime dateUsedFrom;
    @JsonProperty("date_used_to") OffsetDateTime dateUsedTo;
    @JsonProperty("offence_oas") String offenceOas;
    @JsonProperty("offence_oas_cy") String offenceOasC;
}
