package uk.gov.hmcts.opal.config;

import jakarta.jms.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import uk.gov.hmcts.opal.common.config.ServiceBusConnectionStringParser;

@EnableJms
@Configuration
@RequiredArgsConstructor
public class AcknowledgementsQueuePublisherJmsConfig {
    private final ServiceBusConnectionStringParser serviceBusConnectionStringParser;

    @Bean("acknowledgementsPublisherConnectionFactory")
    public ConnectionFactory acknowledgementsPublisherConnectionFactory(ServiceBusProperties commonProperties) {
        ServiceBusConnectionStringParser.ConnectionDetails details =
            serviceBusConnectionStringParser.parse(commonProperties.getConnectionString());

        String remoteUri = "%s://%s?jms.sendTimeout=%d&amqp.idleTimeout=%d".formatted(
            commonProperties.getProtocol(),
            details.fullyQualifiedNamespace(),
            commonProperties.getSendTimeoutMs(),
            commonProperties.getIdleTimeoutMs()
        );

        JmsConnectionFactory qpidFactory = new JmsConnectionFactory(remoteUri);
        qpidFactory.setUsername(details.sharedAccessKeyName());
        qpidFactory.setPassword(details.sharedAccessKey());

        CachingConnectionFactory cachingFactory = new CachingConnectionFactory(qpidFactory);
        cachingFactory.setCacheProducers(true);
        cachingFactory.setReconnectOnException(true);
        return cachingFactory;
    }

    @Bean("acknowledgementsPublisherJmsTemplate")
    public JmsTemplate acknowledgementsPublisherJmsTemplate(
        @Qualifier("acknowledgementsPublisherConnectionFactory") ConnectionFactory connectionFactory) {
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setDeliveryPersistent(true);
        jmsTemplate.setExplicitQosEnabled(true);
        return jmsTemplate;
    }
}
