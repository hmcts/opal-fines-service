package uk.gov.hmcts.opal.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.jms.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

class QueueConsumerJmsConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(QueueConsumerJmsConfig.class);

    @Test
    void loadsJmsBeansWhenEnabled() {
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