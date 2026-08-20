package uk.gov.hmcts.opal.service.hmrc.clients.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class HmrcAuthToken {

    private String accessToken;
    private String tokenType;
    private int expiresIn;
    private String scope;
}
