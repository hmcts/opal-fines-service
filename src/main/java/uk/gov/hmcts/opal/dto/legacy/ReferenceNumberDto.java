package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReferenceNumberDto {

    @JsonProperty("organisation")
    @NotNull
    private Boolean organisation;

    @JsonProperty("account_number")
    @NotNull
    private String accountNumber;

    @JsonProperty("prosecutor_case_reference")
    @NotNull
    private String prosecutorCaseReference;
}