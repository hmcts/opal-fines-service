package uk.gov.hmcts.opal.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostedDetails {

    @JsonProperty("posted_date")
    private LocalDate postedDate;

    @JsonProperty("posted_by")
    private String postedBy;

    @JsonProperty("posted_by_name")
    private String postedByName;
}
