package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import uk.gov.hmcts.opal.util.KeepAsJsonDeserializer;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddDraftAccountRequestDto implements ToJsonString, DraftAccountRequestDto {

    @JsonProperty("draft_account_id")
    private Long draftAccountId;

    @JsonProperty("created_at")
    private OffsetDateTime createdDate;

    @JsonProperty("validated_at")
    private OffsetDateTime validatedDate;

    @JsonProperty(value = "business_unit_id", required = true)
    @NonNull
    private Short businessUnitId;

    @JsonProperty("validated_by")
    private String validatedBy;

    @JsonProperty(value = "account", required = true)
    @JsonDeserialize(using = KeepAsJsonDeserializer.class)
    @JsonRawValue
    private String account;

    @JsonProperty("account_snapshot")
    @JsonDeserialize(using = KeepAsJsonDeserializer.class)
    @JsonRawValue
    private String accountSnapshot;

    @JsonProperty(value = "account_type", required = true)
    @NotBlank(message = "account_type must not be blank")
    private String accountType;

    @JsonProperty(value = "timeline_data", required = true)
    @JsonDeserialize(using = KeepAsJsonDeserializer.class)
    @JsonRawValue
    private String timelineData;

    @JsonProperty(value = "submitted_by", required = true)
    private String submittedBy;

    @JsonProperty(value = "submitted_by_name", required = true)
    private String submittedByName;
}
