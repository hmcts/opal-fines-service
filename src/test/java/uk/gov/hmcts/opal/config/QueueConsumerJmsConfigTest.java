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
import uk.gov.hmcts.opal.service.messaging.ReportQueueConsumerService;
import uk.gov.hmcts.opal.service.messaging.ReportQueueListener;

@ExtendWith(MockitoExtension.class)
class QueueConsumerJmsConfigTest {

    private final ServiceBusConnectionStringParser serviceBusConnectionStringParser =
        mock(ServiceBusConnectionStringParser.class);

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withBean(ServiceBusConnectionStringParser.class, () -> serviceBusConnectionStringParser)
        .withBean(ReportQueueConsumerService.class, () -> mock(ReportQueueConsumerService.class))
        .withUserConfiguration(QueueConsumerJmsConfig.class, ReportQueueListener.class);

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
                "opal.report.service-bus.consumer-enabled=true",
                "opal.common.service-bus.connection-string=Endpoint=sb://example.servicebus.windows.net/;"
                    + "SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=key",
                "opal.report.service-bus.queue-name=report"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(QueueConsumerJmsConfig.class);
                assertThat(context).hasSingleBean(ConnectionFactory.class);
                assertThat(context).hasSingleBean(DefaultJmsListenerContainerFactory.class);
                assertThat(context).hasSingleBean(ReportQueueListener.class);
            });
    }

    @Test
    void skipsJmsBeansWhenDisabled() {
        contextRunner
            .withPropertyValues("opal.report.service-bus.consumer-enabled=false")
            .run(context -> {
                assertThat(context).doesNotHaveBean(QueueConsumerJmsConfig.class);
                assertThat(context).doesNotHaveBean(ConnectionFactory.class);
                assertThat(context).doesNotHaveBean(ReportQueueListener.class);
            });
    }

}

