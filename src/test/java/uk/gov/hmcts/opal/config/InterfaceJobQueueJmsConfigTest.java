package uk.gov.hmcts.opal.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.jms.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import uk.gov.hmcts.opal.config.ServiceBusConnectionStringParser.ConnectionDetails;
import uk.gov.hmcts.opal.service.messaging.InterfaceJobQueueConsumerService;
import uk.gov.hmcts.opal.service.messaging.InterfaceJobQueueListener;

@ExtendWith(MockitoExtension.class)
class InterfaceJobQueueJmsConfigTest {

    private final ServiceBusConnectionStringParser serviceBusConnectionStringParser =
        mock(ServiceBusConnectionStringParser.class);

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withBean(ServiceBusConnectionStringParser.class, () -> serviceBusConnectionStringParser)
        .withBean(InterfaceJobQueueConsumerService.class, () -> mock(InterfaceJobQueueConsumerService.class))
        .withUserConfiguration(InterfaceJobQueueJmsConfig.class, InterfaceJobQueueListener.class);

    @Test
    void loadsJmsBeansWhenEnabled() {
        when(serviceBusConnectionStringParser.parse(anyString())).thenReturn(
            new ConnectionDetails("example.servicebus.windows.net",
                "RootManageSharedAccessKey", "key"));

        contextRunner.withPropertyValues("opal.interface-jobs.service-bus.consumer-enabled=true",
            "opal.common.service-bus.connection-string=Endpoint=sb://example.servicebus.windows.net/;"
                + "SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=key",
            "opal.interface-jobs.service-bus.queue-name=auto-payments-process-interface-files")
            .run(context -> {
                assertThat(context).hasSingleBean(InterfaceJobQueueJmsConfig.class);
                assertThat(context).hasSingleBean(ConnectionFactory.class);
                assertThat(context).hasSingleBean(DefaultJmsListenerContainerFactory.class);
                assertThat(context).hasSingleBean(InterfaceJobQueueListener.class);
            });
    }

    @Test
    void skipsJmsBeansWhenDisabled() {
        contextRunner.withPropertyValues("opal.interface-jobs.service-bus.consumer-enabled=false")
            .run(context -> {
                assertThat(context).doesNotHaveBean(QueueConsumerJmsConfig.class);
                assertThat(context).doesNotHaveBean(ConnectionFactory.class);
                assertThat(context).doesNotHaveBean(InterfaceJobQueueListener.class);
            });
    }

    @Test
    void skipsJmsBeanWhenConsumerIsEnabledAndAutomatedJob() {
        contextRunner
            .withPropertyValues("opal.interface-jobs.service-bus.consumer-enabled=true",
                "opal.automated-task=true")
            .run(context -> {
                assertThat(context).doesNotHaveBean(InterfaceJobQueueJmsConfig.class);
                assertThat(context).doesNotHaveBean(ConnectionFactory.class);
                assertThat(context).doesNotHaveBean(InterfaceJobQueueListener.class);
            });
    }
}
