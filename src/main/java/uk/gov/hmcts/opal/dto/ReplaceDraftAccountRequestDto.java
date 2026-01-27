package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.util.KeepAsJsonDeserializer;
import uk.gov.hmcts.opal.util.Versioned;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReplaceDraftAccountRequestDto implements ToJsonString, DraftAccountRequestDto, Versioned {

    @NotNull
    @JsonProperty(value = "business_unit_id", required = true)
    private Short businessUnitId;

    @JsonProperty(value = "submitted_by", required = true)
    private String submittedBy;

    @JsonProperty(value = "submitted_by_name", required = true)
    private String submittedByName;

    @NotBlank
    @JsonProperty(value = "account", required = true)
    @JsonDeserialize(using = KeepAsJsonDeserializer.class)
    @JsonRawValue
    private String account;

    @NotBlank
    @JsonProperty(value = "account_type", required = true)
    private String accountType;

    @JsonProperty(value = "account_status", required = true)
    private String accountStatus;

    @NotBlank
    @JsonProperty(value = "timeline_data", required = true)
    @JsonDeserialize(using = KeepAsJsonDeserializer.class)
    @JsonRawValue
    private String timelineData;

    @JsonIgnore
    private BigInteger version;
}
