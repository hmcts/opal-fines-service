package uk.gov.hmcts.opal.config;

import jakarta.jms.ConnectionFactory;
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
@EnableConfigurationProperties(QueueConsumerProperties.class)
@ConditionalOnProperty(prefix = "opal.report.consumer", name = "enabled", havingValue = "true")
public class QueueConsumerJmsConfig {

    @Bean
    public ConnectionFactory reportConsumerConnectionFactory(QueueConsumerProperties properties) {
        ServiceBusConnectionStringParser.ConnectionDetails details =
            ServiceBusConnectionStringParser.parse(properties.getConnectionString());

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
