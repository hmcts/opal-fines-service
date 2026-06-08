package uk.gov.hmcts.opal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "opal.report.service-bus")
@Data
public class ReportServiceBusProperties {
    private String queueName;
    private boolean consumerEnabled;
    private boolean publisherEnabled;
}
