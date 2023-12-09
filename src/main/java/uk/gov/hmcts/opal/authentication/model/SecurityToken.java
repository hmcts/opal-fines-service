package uk.gov.hmcts.opal.authentication.model;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class SecurityToken {

    private String accessToken;

}
