package uk.gov.hmcts.opal.authentication.model;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.opal.authorisation.model.UserState;

@Builder
@Value
public class SecurityToken {

    private String accessToken;
    private UserState userState;

}
