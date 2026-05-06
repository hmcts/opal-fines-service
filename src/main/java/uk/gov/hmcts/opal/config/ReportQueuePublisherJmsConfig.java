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
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

@EnableJms
@Configuration
@EnableConfigurationProperties(ReportQueuePublisherProperties.class)
@ConditionalOnProperty(prefix = "opal.report.publisher", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class ReportQueuePublisherJmsConfig {
    private final ServiceBusConnectionStringParser serviceBusConnectionStringParser;

    @Bean("reportPublisherConnectionFactory")
    public ConnectionFactory reportPublisherConnectionFactory(ReportQueuePublisherProperties properties) {
        ServiceBusConnectionStringParser.ConnectionDetails details =
            serviceBusConnectionStringParser.parse(properties.getConnectionString());

        String remoteUri = "%s://%s?jms.sendTimeout=%d&amqp.idleTimeout=%d".formatted(
            properties.getProtocol(),
            details.fullyQualifiedNamespace(),
            properties.getSendTimeoutMs(),
            properties.getIdleTimeoutMs()
        );

        JmsConnectionFactory qpidFactory = new JmsConnectionFactory(remoteUri);
        qpidFactory.setUsername(details.sharedAccessKeyName());
        qpidFactory.setPassword(details.sharedAccessKey());

        CachingConnectionFactory cachingFactory = new CachingConnectionFactory(qpidFactory);
        cachingFactory.setCacheProducers(true);
        cachingFactory.setReconnectOnException(true);
        return cachingFactory;
    }

    @Bean("reportPublisherJmsTemplate")
    public JmsTemplate reportPublisherJmsTemplate(
        @Qualifier("reportPublisherConnectionFactory") ConnectionFactory connectionFactory) {
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setDeliveryPersistent(true);
        jmsTemplate.setExplicitQosEnabled(true);
        return jmsTemplate;
    }
}
