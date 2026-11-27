package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.service.opal.till.AllocateTillsStatusService;

@ExtendWith(MockitoExtension.class)
class AllocateTillsQueueConsumerServiceTest {

    private static final String TILL_PAYLOAD = """
        {"till_id":123,
        "business_unit_id":78,
        "posted_by":"USER01",
        "posted_by_name":"Opal User",
        "requested_by":42}
        """;
    private static final Long TILL_ID = 123L;
    private static final TillQueueMessage TILL_MESSAGE =
        new TillQueueMessage(TILL_ID, (short) 78, "USER01", "Opal User", 42L);

    @Mock
    private AllocateTillsStatusService statusService;

    @Mock
    private AllocateTillsQueueProcessingService processingService;

    @Mock
    private TransientFailureHelper transientFailureHelper;

    private AllocateTillsQueueConsumerService consumer;

    @BeforeEach
    void setUp() {
        consumer = new AllocateTillsQueueConsumerService(
            new ObjectMapper(), statusService, processingService, transientFailureHelper);
    }

    @Test
    void consume_allocatesProcessingTill() {
        when(statusService.isProcessing(TILL_ID)).thenReturn(true);

        consumer.consume(TILL_PAYLOAD);

        verify(processingService).process(TILL_MESSAGE);
        verify(statusService).isProcessing(TILL_ID);
    }

    @Test
    void consume_rejectsTillThatIsNotProcessing() {
        when(statusService.isProcessing(TILL_ID)).thenReturn(false);

        assertThatThrownBy(() -> consumer.consume(TILL_PAYLOAD))
            .isInstanceOf(TillNotProcessingException.class)
            .hasMessage("Till 123 is not PROCESSING");
        verifyNoInteractions(processingService);
        verify(statusService).isProcessing(TILL_ID);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void consume_rejectsBlankPayload(String payload) {
        assertThatThrownBy(() -> consumer.consume(payload))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Till queue message payload is blank");
        verifyNoInteractions(processingService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"till_id\":null}", "{\"till_id\":0}", "{\"till_id\":-1}", "null"})
    void consume_rejectsMissingOrNonPositiveTillId(String payload) {
        assertThatThrownBy(() -> consumer.consume(payload))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Till queue message must contain a positive till_id");
        verifyNoInteractions(processingService);
    }

    @Test
    void consume_rejectsMalformedJson() {
        assertThatThrownBy(() -> consumer.consume("{invalid"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Unable to parse till queue message payload");
        verifyNoInteractions(processingService);
    }

    @Test
    void consume_rejectsNonNumericTillId() {
        assertThatThrownBy(() -> consumer.consume("{\"till_id\":\"not-a-number\"}"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Unable to parse till queue message payload");
        verifyNoInteractions(processingService);
    }

    @Test
    void retriesTransientProcessingFailure() {
        when(statusService.isProcessing(TILL_ID)).thenReturn(true);
        RuntimeException failure = new RuntimeException("Temporary failure");
        doThrow(failure).when(processingService).process(TILL_MESSAGE);
        when(transientFailureHelper.isTransientTillAllocationFailure(failure)).thenReturn(true);

        assertThatThrownBy(() -> consumer.consume(TILL_PAYLOAD)).isSameAs(failure);
        verify(statusService, never()).markFailed(TILL_ID);
    }

    @Test
    void savesFailedForNonTransientFailure() {
        when(statusService.isProcessing(TILL_ID)).thenReturn(true);
        doThrow(new IllegalArgumentException("Invalid data")).when(processingService).process(TILL_MESSAGE);

        consumer.consume(TILL_PAYLOAD);

        verify(statusService).markFailed(TILL_ID);
    }

    @Test
    void propagatesFailedStatusPersistenceFailure() {
        when(statusService.isProcessing(TILL_ID)).thenReturn(true);
        doThrow(new IllegalArgumentException("Invalid data")).when(processingService).process(TILL_MESSAGE);
        RuntimeException failure = new RuntimeException("Database unavailable");
        doThrow(failure).when(statusService).markFailed(TILL_ID);

        assertThatThrownBy(() -> consumer.consume(TILL_PAYLOAD)).isSameAs(failure);
    }
}
