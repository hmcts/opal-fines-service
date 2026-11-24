package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PublishDraftAccountListenerTest {
    @Mock
    private TextMessage textMessage;

    @Mock
    private PublishDraftAccountConsumerService consumerService;

    @InjectMocks
    private PublishDraftAccountListener listener;


    @Test
    void shouldProcessPublishDraftAccountMessage() throws Exception {

        String payload = """
            {"draft_account_id": 1}""";

        when(textMessage.getText()).thenReturn(payload);

        listener.onMessage(textMessage);

        verify(consumerService)
            .consume(eq(payload));
    }

    @Test
    void shouldThrowExceptionWhenMessageIsNotTextMessage() {
        Message message = mock(Message.class);

        assertThatThrownBy(() -> listener.onMessage(message))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Message must be of type TextMessage or JmsBytesMessage");
    }

}
