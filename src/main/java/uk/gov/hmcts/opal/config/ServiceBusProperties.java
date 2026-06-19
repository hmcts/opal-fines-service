package uk.gov.hmcts.opal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the asynchronous common instance publisher queue.
 * Values are injected from {@code opal.common.service-bus.*}.
 */
@ConfigurationProperties(prefix = "opal.common.service-bus")
@Data
public class ServiceBusProperties {

    private String connectionString;
    private String protocol = "amqp";
    private long idleTimeoutMs = 30000;
    private long sendTimeoutMs = 10000;
}
