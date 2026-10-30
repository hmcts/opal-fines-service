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
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-07T10:15:30Z"), ZoneOffset.UTC);
    private static final UserState USER_STATE = UserState.builder().userId(42L).build();

    @Mock
    private LoggingService loggingService;

    private DefendantAccountSearchPdplLoggingService service;

    @BeforeEach
    void setUp() {
        service = new DefendantAccountSearchPdplLoggingService(loggingService, CLOCK);
    }

    @Nested
    class LogSearchResults {

        @ParameterizedTest
        @ValueSource(booleans = {false, true})
        void whenIndividualResultsReturned_queuesConsultationWithUniqueDefendants(boolean consolidationSearch) {
            DefendantAccountSearchResultsDto results = results(
                individual("1001"),
                organisation("2001"),
                individual("1002"),
                individual("1001"));
            ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
                ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
            when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

            service.logSearchResults(USER_STATE, results, consolidationSearch);

            verify(loggingService).personalDataAccessLogAsync(captor.capture());
            PersonalDataProcessingLogDetails details = captor.getValue();
            List<ParticipantIdentifier> individuals = details.getIndividuals();

            assertAll(
                () -> assertEquals(consolidationSearch
                    ? "Search Account Results - Consolidation - Individual Defendants"
                    : "Search Account Results - Individual Defendants",
                    details.getBusinessIdentifier()),
                () -> assertEquals(PersonalDataProcessingCategory.CONSULTATION, details.getCategory()),
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
        @NullSource
        @MethodSource("noLoggingResults")
        void whenNoIndividualResultsReturned_doesNotQueueLog_happyPath(DefendantAccountSearchResultsDto results) {
            service.logSearchResults(USER_STATE, results, true);
            service.logSearchResults(USER_STATE, results, false);

            verify(loggingService, never()).personalDataAccessLogAsync(any());
        }

        @ParameterizedTest
        @ValueSource(booleans = {false, true})
        void whenQueueRejects_throws(boolean consolidationSearch) {
            when(loggingService.personalDataAccessLogAsync(any())).thenReturn(false);

            assertThrows(PdplLoggingException.class,
                () -> service.logSearchResults(USER_STATE, results(individual("1001")), consolidationSearch));

            verify(loggingService).personalDataAccessLogAsync(any());
        }

        @ParameterizedTest
        @ValueSource(booleans = {false, true})
        void whenQueueThrows_preservesCause(boolean consolidationSearch) {
            RuntimeException failure = new RuntimeException("logging unavailable");
            when(loggingService.personalDataAccessLogAsync(any())).thenThrow(failure);

            PdplLoggingException exception = assertThrows(PdplLoggingException.class,
                () -> service.logSearchResults(USER_STATE, results(individual("1001")), consolidationSearch));

            assertSame(failure, exception.getCause());
            verify(loggingService).personalDataAccessLogAsync(any());
        }

        private static Stream<DefendantAccountSearchResultsDto> noLoggingResults() {
            return Stream.of(
                DefendantAccountSearchResultsDto.builder().build(),
                DefendantAccountSearchResultsDto.builder().defendantAccounts(List.of()).build(),
                DefendantAccountSearchResultsDto.builder()
                    .defendantAccounts(Collections.singletonList(null))
                    .build(),
                results(organisation("2001"))
            );
        }
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
