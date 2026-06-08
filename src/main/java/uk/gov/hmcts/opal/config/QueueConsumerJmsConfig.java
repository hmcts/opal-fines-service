package uk.gov.hmcts.opal.config;

import jakarta.jms.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;

@EnableJms
@Configuration
@ConditionalOnProperty(prefix = "opal.report.service-bus", name = "consumer-enabled", havingValue = "true")
@EnableConfigurationProperties(ServiceBusProperties.class)
@RequiredArgsConstructor
public class QueueConsumerJmsConfig {

    private final ServiceBusConnectionStringParser serviceBusConnectionStringParser;

    @Bean
    public ConnectionFactory reportConsumerConnectionFactory(ServiceBusProperties properties) {
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

    @Bean
    public DefaultJmsListenerContainerFactory reportListenerContainerFactory(
        ConnectionFactory reportConsumerConnectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(reportConsumerConnectionFactory);
        factory.setSessionTransacted(true);
        return factory;
    }

}
