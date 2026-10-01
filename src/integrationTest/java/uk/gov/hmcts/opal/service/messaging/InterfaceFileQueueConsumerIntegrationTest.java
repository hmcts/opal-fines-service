package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.service.InterfaceFileProcessorService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@DisplayName("Interface File Queue Consumer Integration Tests")
class InterfaceFileQueueConsumerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private InterfaceFileQueueConsumerService consumer;

    @MockitoBean
    private InterfaceFileProcessorService interfaceFileProcessorService;

    @Test
    @JiraStory("PO-6459")
    @JiraEpic("PO-6390")
    @DisplayName("AC1 - Valid message invokes processor service")
    void ac1ValidMessageInvokesProcessorService() {

        assertThatCode(() ->
                           consumer.consume("""
            {
              "interfaceFileId": 1
            }
            """)
        ).doesNotThrowAnyException();

        verify(interfaceFileProcessorService)
            .process(1L);
    }

    @Test
    @JiraStory("PO-6459")
    @JiraEpic("PO-6390")
    @DisplayName("AC2 - Processing failure is propagated")
    void ac2ProcessingFailureIsPropagated() {

        doThrow(new RuntimeException("processing failure"))
            .when(interfaceFileProcessorService)
            .process(1L);

        assertThatThrownBy(() ->
                               consumer.consume("""
                {
                  "interfaceFileId": 1
                }
                """)
        )
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("processing failure");
    }

    @Test
    @JiraStory("PO-6459")
    @JiraEpic("PO-6390")
    @DisplayName("Invalid payload throws exception")
    void invalidPayloadThrowsException() {

        assertThatThrownBy(() -> consumer.consume("{invalid"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unable to parse");
    }

    @Test
    @JiraStory("PO-6459")
    @JiraEpic("PO-6390")
    @DisplayName("Blank payload throws exception")
    void blankPayloadThrowsException() {

        assertThatThrownBy(() -> consumer.consume(" "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("payload is blank");
    }
}
