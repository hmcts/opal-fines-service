package uk.gov.hmcts.opal.config;

import jakarta.jms.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;

@EnableJms
@Configuration
@ConditionalOnProperty(prefix = "opal.ref-data.service-bus", name = "consumer-enabled", havingValue = "true")
@EnableConfigurationProperties({ServiceBusProperties.class, RefDataServiceBusProperties.class})
@RequiredArgsConstructor
public class RefDataTopicConsumerJmsConfig {

    private final ServiceBusConnectionStringParser serviceBusConnectionStringParser;

    @Bean("refDataTopicConsumerConnectionFactory")
    public ConnectionFactory refDataTopicConsumerConnectionFactory(ServiceBusProperties properties) {
        ServiceBusConnectionStringParser.ConnectionDetails details =
            serviceBusConnectionStringParser.parse(properties.getConnectionString());

        String remoteUri = "%s://%s?amqp.idleTimeout=%d".formatted(
            properties.getProtocol(),
            details.fullyQualifiedNamespace(),
            properties.getIdleTimeoutMs()
        );

        JmsConnectionFactory qpidFactory = new JmsConnectionFactory(remoteUri);
        qpidFactory.setUsername(details.sharedAccessKeyName());
        qpidFactory.setPassword(details.sharedAccessKey());
        return new CachingConnectionFactory(qpidFactory);
    }

    @Bean("refDataTopicListenerContainerFactory")
    public DefaultJmsListenerContainerFactory refDataTopicListenerContainerFactory(
        @Qualifier("refDataTopicConsumerConnectionFactory") ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setSessionTransacted(true);
        factory.setSubscriptionShared(true);
        factory.setSubscriptionDurable(true);
        return factory;
    }
}
