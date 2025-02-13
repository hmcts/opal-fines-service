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
public class UpdateDraftAccountRequestDto implements ToJsonString {

    @JsonProperty("validated_by")
    private String validatedBy;

    @JsonProperty("validated_by_name")
    private String validatedByName;

    @JsonProperty("business_unit_id")
    private Short businessUnitId;

    @JsonProperty("account_status")
    private String accountStatus;

    @JsonProperty("timeline_data")
    @JsonDeserialize(using = KeepAsJsonDeserializer.class)
    @JsonRawValue
    private String timelineData;

    @JsonProperty(value = "version")
    private Long version;

}
