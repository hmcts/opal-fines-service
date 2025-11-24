package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.util.KeepAsJsonDeserializer;
import uk.gov.hmcts.opal.util.Versioned;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // This line forces the HTTP Response to be of type 'application/json'
public class DraftAccountResponseDto implements ToJsonString, Versioned {

    @JsonProperty("draft_account_id")
    private Long draftAccountId;

    @JsonProperty("business_unit_id")
    private Short businessUnitId;

    @JsonProperty("created_at")
    private OffsetDateTime createdDate;

    @JsonProperty("submitted_by")
    private String submittedBy;

    @JsonProperty("submitted_by_name")
    private String submittedByName;

    @JsonProperty("validated_at")
    private OffsetDateTime validatedDate;

    @JsonProperty("validated_by")
    private String validatedBy;

    @JsonProperty("validated_by_name")
    private String validatedByName;

    @JsonProperty("account")
    @JsonDeserialize(using = KeepAsJsonDeserializer.class)
    @JsonRawValue
    private String account;

    @JsonProperty("account_snapshot")
    @JsonDeserialize(using = KeepAsJsonDeserializer.class)
    @JsonRawValue
    private String accountSnapshot;

    @JsonProperty("account_type")
    private String accountType;

    @JsonProperty("account_status")
    private DraftAccountStatus accountStatus;

    @JsonProperty("status_message")
    private String statusMessage;

    @JsonProperty("account_status_date")
    private OffsetDateTime accountStatusDate;

    @JsonProperty("timeline_data")
    @JsonDeserialize(using = KeepAsJsonDeserializer.class)
    @JsonRawValue
    private String timelineData;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("account_id")
    private Long accountId;

    @JsonIgnore
    private BigInteger version;
}
