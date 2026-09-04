package uk.gov.hmcts.opal.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import jakarta.jms.ConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import uk.gov.hmcts.opal.config.ServiceBusConnectionStringParser.ConnectionDetails;

@ExtendWith(MockitoExtension.class)
class InterfaceJobQueueJmsConfigTest {

    @Mock
    private ServiceBusConnectionStringParser serviceBusConnectionStringParser;

    private ApplicationContextRunner contextRunner;

    @BeforeEach
    void setUp() {
        contextRunner = new ApplicationContextRunner()
            .withBean(ServiceBusConnectionStringParser.class, () -> serviceBusConnectionStringParser)
            .withBean(InterfaceJobQueueJmsConfig.class);
    }

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
                assertThat(context.getBean(InterfaceJobQueueJmsConfig.class)).isNotNull();
                assertThat(context).hasSingleBean(ConnectionFactory.class);
                assertThat(context).hasSingleBean(DefaultJmsListenerContainerFactory.class);
        });
    }

    @Test
    void skipsJmsBeansWhenDisabled() {
        contextRunner.withPropertyValues("opal.interface-jobs.service-bus.consumer-enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(ConnectionFactory.class));
    }
}
