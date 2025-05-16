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
import uk.gov.hmcts.opal.util.Versioned;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateDraftAccountRequestDto implements ToJsonString, Versioned {

    @JsonProperty("validated_by")
    private String validatedBy;

    @JsonProperty("validated_by_name")
    private String validatedByName;

    @JsonProperty("business_unit_id")
    private Short businessUnitId;

    @JsonProperty("account_status")
    private String accountStatus;

    @JsonProperty("reason_text")
    private String reasonText;


    @JsonProperty("timeline_data")
    @JsonDeserialize(using = KeepAsJsonDeserializer.class)
    @JsonRawValue
    private String timelineData;

    @JsonProperty(value = "version")
    @NonNull
    private Long version;

}
