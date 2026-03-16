package uk.gov.hmcts.opal;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Data
@Profile("integration-with-spring-security")
public class SecurityItProperties {

    @Value("${spring.security.oauth2.client.provider.internal-azure-ad-provider.jwk-set-uri}")
    private String wireMockJwksUri;

    @Value("${security-it.key}")
    private String key;

    @Value("${spring.security.oauth2.client.registration.internal-azure-ad.issuer-uri}")
    private String issuerUri;
}
