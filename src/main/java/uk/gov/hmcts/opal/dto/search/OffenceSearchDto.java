package uk.gov.hmcts.opal.dto.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;

import java.time.OffsetDateTime;

@Data
@Builder
public class OffenceSearchDto implements ToJsonString {

    @JsonProperty("offence_id")
    private String offenceId;

    @JsonProperty("cjs_code")
    private String cjsCode;

    @JsonProperty("title")
    private String title;

    @JsonProperty("act_and_section")
    private String actSection;

    @JsonProperty("active_date")
    private OffsetDateTime activeDate;

    @JsonProperty("max_results")
    private Integer maxResults;
}
