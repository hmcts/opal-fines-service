package uk.gov.hmcts.opal.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "legacy-gateway")
public class LegacyGatewayProperties {

    private String url;
    private String username;
    private String password;
}
