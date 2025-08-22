package uk.gov.hmcts.opal.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountStatusReference {

    @JsonProperty("account_status_code")
    private String accountStatusCode;

    @JsonProperty("account_status_display_name")
    private String accountStatusDisplayName;
}
