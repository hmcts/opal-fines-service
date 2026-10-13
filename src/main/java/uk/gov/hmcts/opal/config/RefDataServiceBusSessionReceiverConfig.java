package uk.gov.hmcts.opal.config;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSessionReceiverClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "opal.ref-data.service-bus", name = "consumer-enabled", havingValue = "true")
@EnableConfigurationProperties(RefDataServiceBusProperties.class)
public class RefDataServiceBusSessionReceiverConfig {

    static final String MISSING_FULLY_QUALIFIED_NAMESPACE_MESSAGE =
        "opal.ref-data.service-bus.fully-qualified-namespace must be set when connection-string is blank";

    @Bean(name = "refDataServiceBusSessionReceiverClient", destroyMethod = "close")
    public ServiceBusSessionReceiverClient refDataServiceBusSessionReceiverClient(
        RefDataServiceBusProperties refDataServiceBusProperties) {

        //PEEK_LOCK — message is received but remains in Service Bus under a lock until completed/abandoned/dead-lettered or the lock expires.
        //RECEIVE_AND_DELETE — message is removed from Service Bus as soon as Service Bus transfers it to the receiver. There is no lock/redelivery if your processing subsequently fails.
        //we want peek lock

        ServiceBusClientBuilder builder = new ServiceBusClientBuilder();
        String connectionString = refDataServiceBusProperties.getConnectionString();

        if (connectionString != null && !connectionString.isBlank()) {
            builder.connectionString(connectionString);
        } else {
            String fullyQualifiedNamespace = refDataServiceBusProperties.getFullyQualifiedNamespace();
            if (fullyQualifiedNamespace == null || fullyQualifiedNamespace.isBlank()) {
                throw new IllegalStateException(MISSING_FULLY_QUALIFIED_NAMESPACE_MESSAGE);
            }
            builder.credential(
                fullyQualifiedNamespace,
                new DefaultAzureCredentialBuilder().build()
            );
        }

        return builder
            .sessionReceiver()
            .topicName(refDataServiceBusProperties.getTopicName())
            .subscriptionName(refDataServiceBusProperties.getSubscriptionName())
            .disableAutoComplete()
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .buildClient();
    }
}
