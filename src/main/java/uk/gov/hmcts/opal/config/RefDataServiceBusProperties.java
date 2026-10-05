package uk.gov.hmcts.opal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "opal.ref-data.service-bus")
@Data
public class RefDataServiceBusProperties {
    private String topicName;
    private String subscriptionName;
    private boolean consumerEnabled;
}
