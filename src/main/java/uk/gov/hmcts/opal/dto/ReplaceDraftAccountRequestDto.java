package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.util.KeepAsJsonDeserializer;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReplaceDraftAccountRequestDto implements ToJsonString {

    @JsonProperty(value = "business_unit_id", required = true)
    private Integer businessUnitId;

    @JsonProperty(value = "submitted_by", required = true)
    private String submittedBy;

    @JsonProperty("validated_by")
    private String validatedBy;

    @JsonProperty(value = "account", required = true)
    @JsonDeserialize(using = KeepAsJsonDeserializer.class)
    @JsonRawValue
    private String account;

    @JsonProperty(value = "account_type", required = true)
    private String accountType;

    @JsonProperty(value = "account_status", required = true)
    private String accountStatus;

    @JsonProperty(value = "timeline_data", required = true)
    @JsonDeserialize(using = KeepAsJsonDeserializer.class)
    @JsonRawValue
    private String timelineData;
}
