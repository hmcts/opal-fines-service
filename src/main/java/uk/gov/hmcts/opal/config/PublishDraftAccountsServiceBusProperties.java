package uk.gov.hmcts.opal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "opal.publish-draft-accounts.service-bus")
public class PublishDraftAccountsServiceBusProperties {

    private String queueName;
}
