package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
class AccountActivityDto {
    @JsonProperty("activity_id")
    private Integer activityId;

    @JsonProperty("activity_type")
    private String activityType;

    @JsonProperty("activity_type_code")
    private String activityTypeCode;

    @JsonProperty("activity_text")
    private String activityText;

    @JsonProperty("posted_date")
    private String postedDate;

    @JsonProperty("posted_by")
    private String postedBy;

    @JsonProperty("amount")
    private Double amount;
}
