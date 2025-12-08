package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddEnforcementResponse {

    @JsonProperty("defendant_account_id")
    @NotBlank
    private String defendantAccountId;

    @JsonProperty("version")
    private Integer version;

    @JsonProperty("enforcement_id")
    @NotBlank
    private String enforcementId;
}
