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

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddDraftAccountRequestDto implements ToJsonString {

    @JsonProperty("draft_account_id")
    private Long draftAccountId;

    @JsonProperty("created_at")
    private OffsetDateTime createdDate;

    @JsonProperty("validated_at")
    private OffsetDateTime validatedDate;

    @JsonProperty(value = "business_unit_id", required = true)
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
    private String accountType;

    @JsonProperty(value = "timeline_data", required = true)
    @JsonDeserialize(using = KeepAsJsonDeserializer.class)
    @JsonRawValue
    private String timelineData;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("account_id")
    private Long accountId;

    @JsonProperty(value = "submitted_by", required = true)
    private String submittedBy;
}