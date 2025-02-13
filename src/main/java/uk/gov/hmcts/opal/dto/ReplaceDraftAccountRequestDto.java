package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import uk.gov.hmcts.opal.util.KeepAsJsonDeserializer;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReplaceDraftAccountRequestDto implements ToJsonString, DraftAccountRequestDto, Versioned {

    @JsonProperty(value = "business_unit_id", required = true)
    @NonNull
    private Short businessUnitId;

    @JsonProperty(value = "submitted_by", required = true)
    private String submittedBy;

    @JsonProperty(value = "submitted_by_name", required = true)
    private String submittedByName;

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

    @JsonProperty(value = "version")
    private Long version;
}
