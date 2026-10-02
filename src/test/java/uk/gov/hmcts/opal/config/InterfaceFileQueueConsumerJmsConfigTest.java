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
import uk.gov.hmcts.opal.service.messaging.InterfaceFileQueueConsumerService;
import uk.gov.hmcts.opal.service.messaging.InterfaceFileQueueListener;

@ExtendWith(MockitoExtension.class)
class InterfaceFileQueueConsumerJmsConfigTest {

    private final ServiceBusConnectionStringParser serviceBusConnectionStringParser =
        mock(ServiceBusConnectionStringParser.class);

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withBean(ServiceBusConnectionStringParser.class,
                  () -> serviceBusConnectionStringParser)
        .withBean(InterfaceFileQueueConsumerService.class,
                  () -> mock(InterfaceFileQueueConsumerService.class))
        .withUserConfiguration(
            QueueConsumerJmsConfig.class,
            InterfaceFileQueueListener.class
        );

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
                "opal.interface-file.service-bus.consumer-enabled=true",
                "opal.report.service-bus.consumer-enabled=true",
                "opal.common.service-bus.connection-string="
                    + "Endpoint=sb://example.servicebus.windows.net/;"
                    + "SharedAccessKeyName=RootManageSharedAccessKey;"
                    + "SharedAccessKey=key",
                "opal.interface-file.service-bus.queue-name="
                    + "banking-interfaces-preprocess-interface-file-fines"
            )
            .run(context -> {
                assertThat(context)
                    .hasSingleBean(QueueConsumerJmsConfig.class);

                assertThat(context)
                    .hasSingleBean(ConnectionFactory.class);

                assertThat(context)
                    .hasBean("reportListenerContainerFactory");

                assertThat(context)
                    .hasBean("interfaceFileListenerContainerFactory");

                assertThat(context)
                    .hasSingleBean(InterfaceFileQueueListener.class);
            });
    }

    @Test
    void skipsJmsBeansWhenDisabled() {

        contextRunner
            .withPropertyValues(
                "opal.interface-file.service-bus.consumer-enabled=false",
                "opal.report.service-bus.consumer-enabled=false"
            )
            .run(context -> {
                assertThat(context)
                    .doesNotHaveBean(QueueConsumerJmsConfig.class);

                assertThat(context)
                    .doesNotHaveBean(ConnectionFactory.class);

                assertThat(context)
                    .doesNotHaveBean(InterfaceFileQueueListener.class);
            });
    }

    @Test
    void skipsJmsBeansWhenConsumerIsEnabledAndAutomatedJob() {

        contextRunner
            .withPropertyValues(
                "opal.interface-file.service-bus.consumer-enabled=true",
                "opal.report.service-bus.consumer-enabled=true",
                "opal.automated-task=true"
            )
            .run(context -> {
                assertThat(context)
                    .doesNotHaveBean(QueueConsumerJmsConfig.class);

                assertThat(context)
                    .doesNotHaveBean(ConnectionFactory.class);

                assertThat(context)
                    .doesNotHaveBean(InterfaceFileQueueListener.class);
            });
    }
}
