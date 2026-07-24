package uk.gov.hmcts.opal.service.hmrc.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class HMRCAuthToken {

    private String accessToken;
    private String tokenType;
    private int expiresIn;
    private String scope;
}
