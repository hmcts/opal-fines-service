package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

public class VpfpoDraftAccountQueueListenerConditionalTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withBean(VpfpoDraftAccountConsumerService.class, () -> mock(VpfpoDraftAccountConsumerService.class))
        .withUserConfiguration(VpfpoDraftAccountQueueListener.class);

    @Test
    void shouldCreateListenerWhenConsumerEnabledAndAutomatedTaskIsEmpty() {
        contextRunner
            .withPropertyValues("opal.draft-accounts.create.service-bus.consumer-enabled=true", "opal.automated-task=")
            .run(context -> assertThat(context).hasSingleBean(VpfpoDraftAccountQueueListener.class));
    }

    @Test
    void shouldNotCreateListenerWhenConsumerDisabled() {
        contextRunner
            .withPropertyValues("opal.draft-accounts.create.service-bus.consumer-enabled=false", "opal.automated-task=")
            .run(context -> assertThat(context).doesNotHaveBean(VpfpoDraftAccountQueueListener.class));
    }

    @Test
    void shouldNotCreateListenerWhenAutomatedTaskIsSet() {
        contextRunner
            .withPropertyValues("opal.draft-accounts.create.service-bus.consumer-enabled=true",
                "opal.automated-task=true")
            .run(context -> assertThat(context).doesNotHaveBean(VpfpoDraftAccountQueueListener.class));
    }

}
