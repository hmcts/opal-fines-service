package uk.gov.hmcts.opal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "opal.tills.service-bus")
public class TillQueueProperties {

    private String queueName;
}
