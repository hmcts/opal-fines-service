package uk.gov.hmcts.opal.service.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ReportQueueListenerConditionalTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withBean(ReportQueueConsumerService.class, () -> mock(ReportQueueConsumerService.class))
        .withUserConfiguration(ReportQueueListener.class);

    @Test
    void shouldCreateListenerWhenConsumerEnabledAndAutomatedTaskIsEmpty() {
        contextRunner
            .withPropertyValues("opal.report.service-bus.consumer-enabled=true", "opal.automated-task=")
            .run(context -> assertThat(context).hasSingleBean(ReportQueueListener.class));
    }

    @Test
    void shouldNotCreateListenerWhenConsumerDisabled() {
        contextRunner
            .withPropertyValues("opal.report.service-bus.consumer-enabled=false", "opal.automated-task=")
            .run(context -> assertThat(context).doesNotHaveBean(ReportQueueListener.class));
    }

    @Test
    void shouldNotCreateListenerWhenAutomatedTaskIsSet() {
        contextRunner
            .withPropertyValues("opal.report.service-bus.consumer-enabled=true", "opal.automated-task=true")
            .run(context -> assertThat(context).doesNotHaveBean(ReportQueueListener.class));
    }
}
