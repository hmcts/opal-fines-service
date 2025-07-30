package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private Boolean organisation;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("prosecutor_case_reference")
    private String prosecutorCaseReference;
}