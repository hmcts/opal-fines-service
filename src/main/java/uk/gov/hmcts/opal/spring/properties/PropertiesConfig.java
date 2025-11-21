package uk.gov.hmcts.opal.spring.properties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ContentDigestProperties.class)
public class PropertiesConfig {

}