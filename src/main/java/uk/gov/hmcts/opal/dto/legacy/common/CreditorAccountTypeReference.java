package uk.gov.hmcts.opal.dto.legacy.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreditorAccountTypeReference {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("account_type")
    private String accountType;

}

