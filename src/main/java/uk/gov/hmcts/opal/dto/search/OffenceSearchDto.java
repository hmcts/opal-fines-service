package uk.gov.hmcts.opal.dto.search;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.util.LocalDateTimeAdapter;

import java.time.LocalDateTime;

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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    @JsonProperty("active_date")
    private LocalDateTime activeDate;

    @JsonProperty("max_results")
    private Integer maxResults;
}
