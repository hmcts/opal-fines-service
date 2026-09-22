package uk.gov.hmcts.opal.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.opal.service.messaging.RefDataServiceBusMessageConsumer;

@Configuration
@ConditionalOnProperty(prefix = "opal.ref-data.service-bus", name = "consumer-enabled", havingValue = "true")
@EnableConfigurationProperties({ServiceBusProperties.class, RefDataServiceBusProperties.class})
public class RefDataServiceBusSessionProcessorConfig {

    @Bean(name = "refDataServiceBusSessionProcessorClient", initMethod = "start", destroyMethod = "close")
    public ServiceBusProcessorClient refDataServiceBusSessionProcessorClient(
        ServiceBusProperties serviceBusProperties,
        RefDataServiceBusProperties refDataServiceBusProperties,
        RefDataServiceBusMessageConsumer messageConsumer) {

        return new ServiceBusClientBuilder()
            .connectionString(serviceBusProperties.getConnectionString())
            .sessionProcessor()
            .topicName(refDataServiceBusProperties.getTopicName())
            .subscriptionName(refDataServiceBusProperties.getSubscriptionName())
            .disableAutoComplete()
            .processMessage(messageConsumer::consumeMessage)
            .processError(messageConsumer::processError)
            .buildProcessorClient();
    }
}
