package uk.gov.hmcts.opal.service.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class InterfaceJobQueueListenerConditionalTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withBean(InterfaceJobQueueConsumerService.class, () -> mock(InterfaceJobQueueConsumerService.class))
        .withUserConfiguration(InterfaceJobQueueListener.class);

    @Test
    void shouldCreateListenerWhenConsumerEnabledAndAutomatedTaskIsEmpty() {
        contextRunner
            .withPropertyValues("opal.interface-jobs.service-bus.consumer-enabled=true", "opal.automated-task=")
            .run(context -> assertThat(context).hasSingleBean(InterfaceJobQueueListener.class));
    }

    @Test
    void shouldNotCreateListenerWhenConsumerDisabled() {
        contextRunner
            .withPropertyValues("opal.interface-jobs.service-bus.consumer-enabled=false", "opal.automated-task=")
            .run(context -> assertThat(context).doesNotHaveBean(InterfaceJobQueueListener.class));
    }

    @Test
    void shouldNotCreateListenerWhenAutomatedTaskIsSet() {
        contextRunner
            .withPropertyValues("opal.interface-jobs.service-bus.consumer-enabled=true",
                "opal.automated-task=true")
            .run(context -> assertThat(context)
                .doesNotHaveBean(InterfaceJobQueueListener.class));
    }
}
