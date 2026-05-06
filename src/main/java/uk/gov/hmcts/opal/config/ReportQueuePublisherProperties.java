package uk.gov.hmcts.opal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the asynchronous report instance publisher.
 * Values are injected from {@code opal.report.publisher.*}.
 */
@ConfigurationProperties(prefix = "opal.report.publisher")
@Data
public class ReportQueuePublisherProperties {

    private boolean enabled = true;
    private String connectionString;
    private String queueName;
    private String protocol = "amqp";
    private long idleTimeoutMs = 30000;
    private long sendTimeoutMs = 10000;
}
