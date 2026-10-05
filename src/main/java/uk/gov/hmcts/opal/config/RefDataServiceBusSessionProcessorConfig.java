package uk.gov.hmcts.opal.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
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

        //PEEK_LOCK — message is received but remains in Service Bus under a lock until completed/abandoned/dead-lettered or the lock expires.
        //RECEIVE_AND_DELETE — message is removed from Service Bus as soon as Service Bus transfers it to the receiver. There is no lock/redelivery if your processing subsequently fails.
        //we want peek lock

        return new ServiceBusClientBuilder()
            .connectionString(serviceBusProperties.getConnectionString())
            .sessionProcessor()
            .topicName(refDataServiceBusProperties.getTopicName())
            .subscriptionName(refDataServiceBusProperties.getSubscriptionName())
            .disableAutoComplete()
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .processMessage(messageConsumer::consumeMessage)
            .processError(messageConsumer::processError)
            .buildProcessorClient();
    }
}
