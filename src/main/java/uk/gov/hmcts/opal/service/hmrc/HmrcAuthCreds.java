package uk.gov.hmcts.opal.service.hmrc;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class HmrcAuthCreds {

    public HmrcAuthCreds(@Value("${hmrc.auth.client-id}") String clientId,
        @Value("${hmrc.auth.client-secret}") String clientSecret,
        @Value("${hmrc.auth.scope}") String scope,
        @Value("${hmrc.auth.grant-type}") String grantType) {

        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
        this.grantType = grantType;
    }

    private final String clientId;
    private final String clientSecret;
    private final String scope;
    private final String grantType;
}
