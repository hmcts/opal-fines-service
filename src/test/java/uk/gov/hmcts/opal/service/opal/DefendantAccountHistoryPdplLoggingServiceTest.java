package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.common.logging.LogUtil;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.dto.PostedDetails;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryItem;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryResponse;
import uk.gov.hmcts.opal.exception.PdplLoggingException;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

@ExtendWith(MockitoExtension.class)
class DefendantAccountHistoryPdplLoggingServiceTest {

    private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-10-12T10:15:30Z");
    private static final UserState USER_STATE = UserState.builder().userId(42L).build();

    @Mock
    private LoggingService loggingService;

    @Nested
    class LogHistoryItems {

        @Test
        void whenHistoryContainsUsers_queuesConsultationWithUniqueUsers_happyPath() {
            DefendantAccountHistoryResponse response = response(
                historyItem("user-1001"),
                historyItem("user-1002"),
                historyItem("user-1001"),
                historyItem(null),
                historyItem(" "));
            ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
                ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
            when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

            try (MockedStatic<LogUtil> logUtil = Mockito.mockStatic(LogUtil.class)) {
                logUtil.when(LogUtil::getIpAddress).thenReturn("192.0.2.10");

                service().logHistoryItems(USER_STATE, response);
            }

            verify(loggingService).personalDataAccessLogAsync(captor.capture());
            PersonalDataProcessingLogDetails details = captor.getValue();
            List<ParticipantIdentifier> individuals = details.getIndividuals();

            assertAll(
                () -> assertEquals("View Defendant Account History Item", details.getBusinessIdentifier()),
                () -> assertEquals(PersonalDataProcessingCategory.CONSULTATION, details.getCategory()),
                () -> assertEquals("192.0.2.10", details.getIpAddress()),
                () -> assertEquals(NOW, details.getCreatedAt()),
                () -> assertEquals("42", details.getCreatedBy().getIdentifier()),
                () -> assertEquals(PdplIdentifierType.OPAL_USER_ID, details.getCreatedBy().getType()),
                () -> assertEquals(List.of("user-1001", "user-1002"), individuals.stream()
                    .map(ParticipantIdentifier::getIdentifier).toList()),
                () -> assertEquals(List.of(PdplIdentifierType.OPAL_USER_ID, PdplIdentifierType.OPAL_USER_ID),
                    individuals.stream().map(ParticipantIdentifier::getType).toList()),
                () -> assertNull(details.getRecipient())
            );
        }

        @ParameterizedTest
        @NullSource
        @MethodSource("responsesWithoutUsers")
        void whenHistoryContainsNoUsers_doesNotQueueLog_happyPath(DefendantAccountHistoryResponse response) {
            service().logHistoryItems(USER_STATE, response);

            verify(loggingService, never()).personalDataAccessLogAsync(any());
        }

        @Test
        void whenQueueReturnsFalse_throwsPdplLoggingException_sadPath() {
            when(loggingService.personalDataAccessLogAsync(any())).thenReturn(false);
            DefendantAccountHistoryPdplLoggingService pdplLoggingService = service();
            DefendantAccountHistoryResponse response = response(historyItem("user-1001"));

            assertThrows(PdplLoggingException.class,
                () -> pdplLoggingService.logHistoryItems(USER_STATE, response));

            verify(loggingService).personalDataAccessLogAsync(any());
        }

        @Test
        void whenQueueRaisesError_wrapsAsPdplLoggingException_sadPath() {
            RuntimeException queueFailure = new RuntimeException("logging unavailable");
            when(loggingService.personalDataAccessLogAsync(any())).thenThrow(queueFailure);
            DefendantAccountHistoryPdplLoggingService pdplLoggingService = service();
            DefendantAccountHistoryResponse response = response(historyItem("user-1001"));

            PdplLoggingException exception = assertThrows(PdplLoggingException.class,
                () -> pdplLoggingService.logHistoryItems(USER_STATE, response));

            assertAll(
                () -> assertSame(queueFailure, exception.getCause()),
                () -> verify(loggingService).personalDataAccessLogAsync(any())
            );
        }

        private static Stream<DefendantAccountHistoryResponse> responsesWithoutUsers() {
            return Stream.of(
                DefendantAccountHistoryResponse.builder().historyItems(null).build(),
                response(),
                response(DefendantAccountHistoryItem.builder().postedDetails(null).build()),
                response(historyItem(null)),
                response(historyItem(" "))
            );
        }
    }

    private DefendantAccountHistoryPdplLoggingService service() {
        return new DefendantAccountHistoryPdplLoggingService(
            loggingService, Clock.fixed(NOW.toInstant(), ZoneOffset.UTC));
    }

    private static DefendantAccountHistoryResponse response(DefendantAccountHistoryItem... items) {
        return DefendantAccountHistoryResponse.builder().historyItems(List.of(items)).build();
    }

    private static DefendantAccountHistoryItem historyItem(String postedBy) {
        return DefendantAccountHistoryItem.builder()
            .postedDetails(PostedDetails.builder().postedBy(postedBy).build())
            .build();
    }
}
