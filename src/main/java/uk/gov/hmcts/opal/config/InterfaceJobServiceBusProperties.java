package uk.gov.hmcts.opal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "opal.interface-jobs.service-bus")
@Data
public class InterfaceJobServiceBusProperties {

    private String queueName;
}
