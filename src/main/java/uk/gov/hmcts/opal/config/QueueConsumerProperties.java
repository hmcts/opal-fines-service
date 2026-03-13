package uk.gov.hmcts.opal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "opal.report.consumer")
@Data
public class QueueConsumerProperties {

    private boolean enabled;
    private String connectionString;
    private String queueName;
    private String protocol = "amqp";
    private long idleTimeoutMs;

}
