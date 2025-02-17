package uk.gov.hmcts.opal.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "be-developer-config")
public class BeDeveloperConfiguration {
    private String userRolePermissions;
    private String techSpike;
}
