package uk.gov.hmcts.opal.authentication.config.internal;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.authentication.config.AuthProviderConfigurationProperties;

@Component
@ConfigurationProperties("spring.security.oauth2.client.provider.internal-azure-ad-provider")
@Getter
@Setter
@ToString
public class InternalAuthProviderConfigurationProperties implements AuthProviderConfigurationProperties {

    private String authorizationUri;

    private String tokenUri;

    private String jwkSetUri;

    private String logoutUri;

    private String resetPasswordUri;

}
