package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.entity.DraftAccountStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DraftAccountSummaryDto implements ToJsonString {

    @JsonProperty("draft_account_id")
    private Long draftAccountId;

    @JsonProperty("created_at")
    private OffsetDateTime createdDate;

    @JsonProperty("submitted_by")
    private String submittedBy;

    @JsonProperty("business_unit_id")
    private Short businessUnitId;

    @JsonProperty("validated_at")
    private OffsetDateTime validatedDate;

    @JsonProperty("validated_by")
    private String validatedBy;

    @JsonProperty("validated_by_name")
    private String validatedByName;

    @JsonProperty("account_snapshot")
    @JsonRawValue
    private String accountSnapshot;

    @JsonProperty("account_type")
    private String accountType;

    @JsonProperty("account_status")
    private DraftAccountStatus accountStatus;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("account_id")
    private Long accountId;

    @JsonProperty("submitted_by_name")
    private String submittedByName;

    @JsonProperty("account_status_date")
    private LocalDate accountStatusDate;

    @JsonProperty("status_message")
    private String statusMessage;

    @JsonProperty("version_number")
    private Long versionNumber;
}