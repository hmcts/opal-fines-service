package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory.CONSULTATION;

import java.time.Clock;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.exception.PdplLoggingException;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

@ExtendWith(MockitoExtension.class)
class AbstractPdplLoggingServiceTest {

    private static final Long USER_ID = 42L;
    private static final String FAILURE_MESSAGE = "Unable to queue PDPO log";

    @Mock
    private LoggingService loggingService;

    @Nested
    class LogPdplOrThrow {

        @Test
        void whenQueueReturnsFalse_throwsPdplLoggingException_sadPath() {
            when(loggingService.personalDataAccessLogAsync(any())).thenReturn(false);
            TestPdplLoggingService service = service();

            PdplLoggingException exception = assertThrows(PdplLoggingException.class,
                () -> service.logPdplOrThrow(
                    "View Personal Data", CONSULTATION, List.of(), null, USER_ID, FAILURE_MESSAGE));

            assertAll(
                () -> assertEquals(FAILURE_MESSAGE, exception.getMessage()),
                () -> assertNull(exception.getCause()),
                () -> verify(loggingService).personalDataAccessLogAsync(any())
            );
        }

        @Test
        void whenQueueRaisesError_wrapsAsPdplLoggingException_sadPath() {
            RuntimeException queueFailure = new RuntimeException("logging unavailable");
            when(loggingService.personalDataAccessLogAsync(any())).thenThrow(queueFailure);
            TestPdplLoggingService service = service();

            PdplLoggingException exception = assertThrows(PdplLoggingException.class,
                () -> service.logPdplOrThrow(
                    "View Personal Data", CONSULTATION, List.of(), null, USER_ID, FAILURE_MESSAGE));

            assertAll(
                () -> assertEquals(FAILURE_MESSAGE, exception.getMessage()),
                () -> assertSame(queueFailure, exception.getCause()),
                () -> verify(loggingService).personalDataAccessLogAsync(any())
            );
        }
    }

    private TestPdplLoggingService service() {
        return new TestPdplLoggingService(loggingService, Clock.systemUTC());
    }

    private static final class TestPdplLoggingService extends AbstractPdplLoggingService {

        private TestPdplLoggingService(LoggingService loggingService, Clock clock) {
            super(loggingService, clock);
        }
    }
}
