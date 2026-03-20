package uk.gov.hmcts.opal.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import jakarta.jms.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

@ExtendWith(MockitoExtension.class)
class QueueConsumerJmsConfigTest {

    private final ServiceBusConnectionStringParser serviceBusConnectionStringParser =
        Mockito.mock(ServiceBusConnectionStringParser.class);

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withBean(ServiceBusConnectionStringParser.class, () -> serviceBusConnectionStringParser)
        .withUserConfiguration(QueueConsumerJmsConfig.class);

    @Test
    void loadsJmsBeansWhenEnabled() {
        when(serviceBusConnectionStringParser.parse(anyString()))
            .thenReturn(new ServiceBusConnectionStringParser.ConnectionDetails(
                "example.servicebus.windows.net",
                "RootManageSharedAccessKey",
                "key"
            ));

        contextRunner
            .withPropertyValues(
                "opal.report.consumer.enabled=true",
                "opal.report.consumer.connection-string=Endpoint=sb://example.servicebus.windows.net/;"
                    + "SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=key",
                "opal.report.consumer.queue-name=report"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(QueueConsumerJmsConfig.class);
                assertThat(context).hasSingleBean(ConnectionFactory.class);
                assertThat(context).hasSingleBean(DefaultJmsListenerContainerFactory.class);
            });
    }

    @Test
    void skipsJmsBeansWhenDisabled() {
        contextRunner
            .withPropertyValues("opal.report.consumer.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(ConnectionFactory.class));
    }

}