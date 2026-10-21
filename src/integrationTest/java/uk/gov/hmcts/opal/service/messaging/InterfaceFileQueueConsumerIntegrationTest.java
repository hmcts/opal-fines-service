package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.jms.TextMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.service.InterfaceFileProcessorService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration"})
@TestPropertySource(properties = {
    "opal.interface-file.service-bus.consumer-enabled=true",
    "opal.report.service-bus.consumer-enabled=true",
    "opal.automated-task=",
    "opal.interface-file.service-bus.queue-name=test-queue"
})
class InterfaceFileQueueConsumerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private InterfaceFileQueueListener listener;

    @MockitoBean
    private InterfaceFileProcessorService interfaceFileProcessorService;

    @MockitoBean
    private TextMessage textMessage;

    @Test
    @DisplayName("AC1 - Valid message invokes processor service")
    @JiraStory("PO-6459")
    @JiraEpic("PO-6390")
    void ac1ValidMessageInvokesProcessorService() throws Exception {

        when(textMessage.getText()).thenReturn("""
            {
              "interfaceFileId": 1
            }
            """);

        listener.onMessage(textMessage);

        verify(interfaceFileProcessorService)
            .process(1L);
    }

    @Test
    @DisplayName("AC2 - Processing failure is propagated")
    @JiraStory("PO-6459")
    @JiraEpic("PO-6390")
    void ac2ProcessingFailureIsPropagated() throws Exception {

        when(textMessage.getText()).thenReturn("""
            {
              "interfaceFileId": 1
            }
            """);

        doThrow(new RuntimeException("processing failure"))
            .when(interfaceFileProcessorService)
            .process(1L);

        assertThatThrownBy(() -> listener.onMessage(textMessage))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("processing failure");
    }

    @Test
    @DisplayName("Invalid payload throws exception")
    @JiraStory("PO-6459")
    @JiraEpic("PO-6390")
    void invalidPayloadThrowsException() throws Exception {

        when(textMessage.getText()).thenReturn("{invalid");

        assertThatThrownBy(() -> listener.onMessage(textMessage))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unable to parse");
    }

    @Test
    @DisplayName("Blank payload throws exception")
    @JiraStory("PO-6459")
    @JiraEpic("PO-6390")
    void blankPayloadThrowsException() throws Exception {

        when(textMessage.getText()).thenReturn(" ");

        assertThatThrownBy(() -> listener.onMessage(textMessage))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("payload is blank");
    }
}