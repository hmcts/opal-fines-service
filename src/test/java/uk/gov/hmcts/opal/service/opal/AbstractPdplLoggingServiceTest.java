package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory.CONSULTATION;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.common.logging.LogUtil;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.exception.PdplLoggingException;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

@ExtendWith(MockitoExtension.class)
class AbstractPdplLoggingServiceTest {

    private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-10-07T10:15:30Z");
    private static final Clock CLOCK = Clock.fixed(NOW.toInstant(), ZoneOffset.UTC);
    private static final Long USER_ID = 42L;
    private static final String FAILURE_MESSAGE = "Unable to queue PDPO log";

    @Mock
    private LoggingService loggingService;

    @Nested
    class LogPdpl {

        @Test
        void whenLogIsQueued_populatesCommonDetails_happyPath() {
            ParticipantIdentifier individual = participant("1001", PdplIdentifierType.DEFENDANT);
            ParticipantIdentifier recipient = participant("2001", PdplIdentifierType.PAYER);
            List<ParticipantIdentifier> individuals = List.of(individual);
            ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
                ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
            when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

            boolean queued;
            try (MockedStatic<LogUtil> logUtil = Mockito.mockStatic(LogUtil.class)) {
                logUtil.when(LogUtil::getIpAddress).thenReturn("192.0.2.10");
                queued = service().logPdpl(
                    "View Personal Data", CONSULTATION, individuals, recipient, USER_ID);
            }

            verify(loggingService).personalDataAccessLogAsync(captor.capture());
            PersonalDataProcessingLogDetails details = captor.getValue();

            assertAll(
                () -> assertTrue(queued),
                () -> assertEquals("View Personal Data", details.getBusinessIdentifier()),
                () -> assertEquals(CONSULTATION, details.getCategory()),
                () -> assertEquals("192.0.2.10", details.getIpAddress()),
                () -> assertEquals(NOW, details.getCreatedAt()),
                () -> assertEquals("42", details.getCreatedBy().getIdentifier()),
                () -> assertEquals(PdplIdentifierType.OPAL_USER_ID, details.getCreatedBy().getType()),
                () -> assertEquals(individuals, details.getIndividuals()),
                () -> assertEquals(recipient, details.getRecipient())
            );
        }
    }

    @Nested
    class LogPdplOrThrow {

        @ParameterizedTest
        @ValueSource(booleans = {false, true})
        void whenQueueReturnsFalse_throwsPdplLoggingException_sadPath(boolean useUserState) {
            when(loggingService.personalDataAccessLogAsync(any())).thenReturn(false);
            TestPdplLoggingService service = service();

            PdplLoggingException exception = assertThrows(PdplLoggingException.class,
                () -> logPersonalData(service, useUserState));

            assertAll(
                () -> assertEquals(FAILURE_MESSAGE, exception.getMessage()),
                () -> assertNull(exception.getCause()),
                () -> verify(loggingService).personalDataAccessLogAsync(any())
            );
        }

        @ParameterizedTest
        @ValueSource(booleans = {false, true})
        void whenQueueRaisesError_wrapsAsPdplLoggingException_sadPath(boolean useUserState) {
            RuntimeException queueFailure = new RuntimeException("logging unavailable");
            when(loggingService.personalDataAccessLogAsync(any())).thenThrow(queueFailure);
            TestPdplLoggingService service = service();

            PdplLoggingException exception = assertThrows(PdplLoggingException.class,
                () -> logPersonalData(service, useUserState));

            assertAll(
                () -> assertEquals(FAILURE_MESSAGE, exception.getMessage()),
                () -> assertSame(queueFailure, exception.getCause()),
                () -> verify(loggingService).personalDataAccessLogAsync(any())
            );
        }
    }

    private void logPersonalData(TestPdplLoggingService service, boolean useUserState) {
        if (useUserState) {
            service.logPdplOrThrow("View Personal Data", CONSULTATION, List.of(), null,
                UserState.builder().userId(USER_ID).build(), FAILURE_MESSAGE);
        } else {
            service.logPdplOrThrow("View Personal Data", CONSULTATION, List.of(), null, USER_ID, FAILURE_MESSAGE);
        }
    }

    private TestPdplLoggingService service() {
        return new TestPdplLoggingService(loggingService, CLOCK);
    }

    private static ParticipantIdentifier participant(String identifier, PdplIdentifierType type) {
        return ParticipantIdentifier.builder().identifier(identifier).type(type).build();
    }

    private static final class TestPdplLoggingService extends AbstractPdplLoggingService {

        private TestPdplLoggingService(LoggingService loggingService, Clock clock) {
            super(loggingService, clock);
        }
    }
}
