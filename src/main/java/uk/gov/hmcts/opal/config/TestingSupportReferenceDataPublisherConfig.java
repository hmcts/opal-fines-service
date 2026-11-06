package uk.gov.hmcts.opal.config;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.opal.refdata.config.RefDataServiceBusProperties;

@Configuration
@EnableConfigurationProperties({ServiceBusProperties.class, RefDataServiceBusProperties.class})
@ConditionalOnProperty(prefix = "opal.testing-support-endpoints", name = "enabled", havingValue = "true")
public class TestingSupportReferenceDataPublisherConfig {

    @Bean(destroyMethod = "close")
    public ServiceBusSenderClient testingSupportReferenceDataSender(
        ServiceBusProperties commonProperties, RefDataServiceBusProperties refDataProperties) {

        ServiceBusClientBuilder clientBuilder = new ServiceBusClientBuilder();
        if (StringUtils.hasText(refDataProperties.getFullyQualifiedNamespace())) {
            clientBuilder.credential(refDataProperties.getFullyQualifiedNamespace(),
                new DefaultAzureCredentialBuilder().build());
        } else {
            clientBuilder.connectionString(commonProperties.getConnectionString());
        }

        clientBuilder.retryOptions(new AmqpRetryOptions()
            .setTryTimeout(Duration.ofSeconds(refDataProperties.getTryTimeoutSeconds())));

        return clientBuilder.sender()
            .topicName(refDataProperties.getTopicName())
            .buildClient();
    }
}
