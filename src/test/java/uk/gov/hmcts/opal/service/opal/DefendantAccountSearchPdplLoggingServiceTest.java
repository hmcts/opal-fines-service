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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.common.logging.LogUtil;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.DefendantAccountSummaryDto;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.exception.PdplLoggingException;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

@ExtendWith(MockitoExtension.class)
class DefendantAccountSearchPdplLoggingServiceTest {

    private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-10-07T10:15:30Z");
    private static final UserState USER_STATE = UserState.builder().userId(42L).build();

    @Mock
    private LoggingService loggingService;

    @Nested
    class LogSearchResults {

        @Test
        void whenIndividualResultsReturned_queuesConsultationWithUniqueDefendants_happyPath() {
            DefendantAccountSearchResultsDto results = results(
                individual("1001"),
                organisation("2001"),
                individual("1002"),
                individual("1001"));
            ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
                ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
            when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

            try (MockedStatic<LogUtil> logUtil = Mockito.mockStatic(LogUtil.class)) {
                logUtil.when(LogUtil::getIpAddress).thenReturn("192.0.2.10");

                service().logSearchResults(USER_STATE, results);
            }

            verify(loggingService).personalDataAccessLogAsync(captor.capture());
            PersonalDataProcessingLogDetails details = captor.getValue();
            List<ParticipantIdentifier> individuals = details.getIndividuals();

            assertAll(
                () -> assertEquals("Search Account Results - Individual Defendants",
                    details.getBusinessIdentifier()),
                () -> assertEquals(PersonalDataProcessingCategory.CONSULTATION, details.getCategory()),
                () -> assertEquals("192.0.2.10", details.getIpAddress()),
                () -> assertEquals(NOW, details.getCreatedAt()),
                () -> assertEquals("42", details.getCreatedBy().getIdentifier()),
                () -> assertEquals(PdplIdentifierType.OPAL_USER_ID, details.getCreatedBy().getType()),
                () -> assertEquals(List.of("1001", "1002"), individuals.stream()
                    .map(ParticipantIdentifier::getIdentifier).toList()),
                () -> assertEquals(List.of(PdplIdentifierType.DEFENDANT, PdplIdentifierType.DEFENDANT),
                    individuals.stream().map(ParticipantIdentifier::getType).toList()),
                () -> assertNull(details.getRecipient())
            );
        }

        @ParameterizedTest
        @MethodSource("noLoggingResults")
        void whenNoIndividualResultsReturned_doesNotQueueLog_happyPath(DefendantAccountSearchResultsDto results) {
            service().logSearchResults(USER_STATE, results);

            verify(loggingService, never()).personalDataAccessLogAsync(any());
        }

        @Test
        void whenQueueReturnsFalse_throwsPdplLoggingException_sadPath() {
            when(loggingService.personalDataAccessLogAsync(any())).thenReturn(false);
            DefendantAccountSearchPdplLoggingService pdplLoggingService = service();
            DefendantAccountSearchResultsDto searchResults = results(individual("1001"));

            assertThrows(PdplLoggingException.class,
                () -> pdplLoggingService.logSearchResults(USER_STATE, searchResults));

            verify(loggingService).personalDataAccessLogAsync(any());
        }

        @Test
        void whenQueueRaisesError_wrapsAsPdplLoggingException_sadPath() {
            RuntimeException queueFailure = new RuntimeException("logging unavailable");
            when(loggingService.personalDataAccessLogAsync(any())).thenThrow(queueFailure);
            DefendantAccountSearchPdplLoggingService pdplLoggingService = service();
            DefendantAccountSearchResultsDto searchResults = results(individual("1001"));

            PdplLoggingException exception = assertThrows(PdplLoggingException.class,
                () -> pdplLoggingService.logSearchResults(USER_STATE, searchResults));

            assertAll(
                () -> assertSame(queueFailure, exception.getCause()),
                () -> verify(loggingService).personalDataAccessLogAsync(any())
            );
        }

        private static Stream<DefendantAccountSearchResultsDto> noLoggingResults() {
            return Stream.of(
                DefendantAccountSearchResultsDto.builder().defendantAccounts(List.of()).build(),
                results(organisation("2001"))
            );
        }
    }

    private DefendantAccountSearchPdplLoggingService service() {
        return new DefendantAccountSearchPdplLoggingService(
            loggingService, Clock.fixed(NOW.toInstant(), ZoneOffset.UTC));
    }

    private static DefendantAccountSearchResultsDto results(DefendantAccountSummaryDto... accounts) {
        return DefendantAccountSearchResultsDto.builder().defendantAccounts(List.of(accounts)).build();
    }

    private static DefendantAccountSummaryDto individual(String defendantAccountId) {
        return DefendantAccountSummaryDto.builder()
            .defendantAccountId(defendantAccountId)
            .organisation(false)
            .build();
    }

    private static DefendantAccountSummaryDto organisation(String defendantAccountId) {
        return DefendantAccountSummaryDto.builder()
            .defendantAccountId(defendantAccountId)
            .organisation(true)
            .build();
    }
}
