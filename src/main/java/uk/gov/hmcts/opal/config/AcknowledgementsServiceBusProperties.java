package uk.gov.hmcts.opal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "opal.acknowledgements.service-bus")
@Data
public class AcknowledgementsServiceBusProperties {
    private String queueName;
}
