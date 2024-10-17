package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.entity.DraftAccountStatus;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // This line forces the HTTP Response to be of type 'application/json'
public class DraftAccountResponseDto implements ToJsonString {

    @JsonProperty("draft_account_id")
    private Long draftAccountId;

    @JsonProperty("business_unit_id")
    private Short businessUnitId;

    @JsonProperty("created_at")
    private OffsetDateTime createdDate;

    @JsonProperty("submitted_by")
    private String submittedBy;

    @JsonProperty("validated_at")
    private OffsetDateTime validatedDate;

    @JsonProperty("validated_by")
    private String validatedBy;

    @JsonProperty("account")
    @JsonRawValue
    private String account;

    @JsonProperty("account_snapshot")
    @JsonRawValue
    private String accountSnapshot;

    @JsonProperty("account_type")
    private String accountType;

    @JsonProperty("account_status")
    private DraftAccountStatus accountStatus;

    @JsonProperty("timeline_data")
    @JsonRawValue
    private String timelineData;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("account_id")
    private Long accountId;
}
