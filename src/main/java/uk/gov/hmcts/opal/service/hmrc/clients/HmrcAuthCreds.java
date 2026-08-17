package uk.gov.hmcts.opal.service.hmrc.clients;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("opal.hmrc.auth")
@RequiredArgsConstructor
@Getter
public class HmrcAuthCreds {

    private final String clientId;
    private final String clientSecret;
    private final String scope;
    private final String grantType;
}
