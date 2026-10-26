package uk.gov.hmcts.opal.service.opal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.exception.PdplLoggingException;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountSearchDefendant;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountSearchResultMinorCreditor;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

@ExtendWith(MockitoExtension.class)
class MinorCreditorSearchPdplLoggingServiceTest {

    private static final UserState USER = UserState.builder().userId(42L).build();
    private static final Instant NOW = Instant.parse("2026-10-21T10:00:00Z");

    @Mock
    private LoggingService loggingService;

    @Test
    void logsUniqueParticipantsWithConsultationMetadata() {
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);
        MinorCreditorAccountsSearchResponse response = response(
            account("101", false, "201", false),
            account("101", false, "201", false),
            account("102", false, "201", false),
            account("103", true, "202", false),
            account("104", false, "203", true));

        service().logSearchResults(USER, response);

        List<PersonalDataProcessingLogDetails> logs = capturedLogs(2);
        assertLog(logs.get(0), "Search Account Results - Individual Minor Creditors",
            PdplIdentifierType.MINOR_CREDITOR, List.of("101", "102", "104"));
        assertLog(logs.get(1), "Search Account Results - Individual Defendants associated with Minor Creditors",
            PdplIdentifierType.DEFENDANT, List.of("201", "202"));
        assertThat(response.getCreditorAccounts()).hasSize(5);
    }

    @ParameterizedTest
    @CsvSource({"false, true, MINOR_CREDITOR", "true, false, DEFENDANT"})
    void filtersEachPartyIndependently(boolean creditorCompany, boolean defendantCompany, PdplIdentifierType type) {
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        service().logSearchResults(USER, response(account("101", creditorCompany, "201", defendantCompany)));

        PersonalDataProcessingLogDetails details = capturedLogs(1).getFirst();
        assertThat(details.getIndividuals()).hasSize(1);
        assertThat(details.getIndividuals().getFirst().getType()).isEqualTo(type);
    }

    @Test
    void absentDefendantOnlyLogsCreditor() {
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);
        MinorCreditorAccountSearchResultMinorCreditor account = account("101", false, null, null);
        account.setDefendant(null);

        service().logSearchResults(USER, response(account));

        assertLog(capturedLogs(1).getFirst(), "Search Account Results - Individual Minor Creditors",
            PdplIdentifierType.MINOR_CREDITOR, List.of("101"));
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("responsesWithoutIndividuals")
    void noIndividualsDoesNotQueue(MinorCreditorAccountsSearchResponse response) {
        service().logSearchResults(USER, response);

        verifyNoInteractions(loggingService);
    }

    @ParameterizedTest
    @MethodSource("unidentifiableIndividuals")
    void unidentifiableIndividualsPreventSuccess(MinorCreditorAccountsSearchResponse response) {
        assertThrows(PdplLoggingException.class, () -> service().logSearchResults(USER, response));

        verifyNoInteractions(loggingService);
    }

    @ParameterizedTest
    @CsvSource({"1, false", "2, false", "1, true", "2, true"})
    void queueFailurePreventsSuccess(int failingCall, boolean raisesError) {
        AtomicInteger calls = new AtomicInteger();
        RuntimeException failure = new IllegalStateException("Queue unavailable");
        when(loggingService.personalDataAccessLogAsync(any())).thenAnswer(invocation -> {
            if (calls.incrementAndGet() == failingCall) {
                if (raisesError) {
                    throw failure;
                }
                return false;
            }
            return true;
        });

        PdplLoggingException exception = assertThrows(PdplLoggingException.class,
            () -> service().logSearchResults(USER, response(account("101", false, "201", false))));

        assertThat(exception.getMessage()).isEqualTo("Unable to queue PDPO log for minor creditor search");
        assertThat(exception.getCause()).isEqualTo(raisesError ? failure : null);
        verify(loggingService, times(failingCall)).personalDataAccessLogAsync(any());
    }

    private static Stream<MinorCreditorAccountsSearchResponse> responsesWithoutIndividuals() {
        return Stream.of(
            MinorCreditorAccountsSearchResponse.builder().build(),
            response(),
            response(account("101", true, "201", true)),
            response(account("101", true, null, null)),
            response((MinorCreditorAccountSearchResultMinorCreditor) null));
    }

    private static Stream<MinorCreditorAccountsSearchResponse> unidentifiableIndividuals() {
        MinorCreditorAccountSearchResultMinorCreditor missingDefendantId = account("101", false, null, false);
        missingDefendantId.getDefendant().setSurname("SearchDefendant");
        return Stream.of(
            response(account(null, false, "201", false)),
            response(account(" ", false, "201", false)),
            response(account("101", null, "201", false)),
            response(account("101", false, "201", null)),
            response(missingDefendantId));
    }

    private MinorCreditorSearchPdplLoggingService service() {
        return new MinorCreditorSearchPdplLoggingService(loggingService, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private List<PersonalDataProcessingLogDetails> capturedLogs(int count) {
        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
        verify(loggingService, times(count)).personalDataAccessLogAsync(captor.capture());
        return captor.getAllValues();
    }

    private void assertLog(PersonalDataProcessingLogDetails details, String businessIdentifier,
                           PdplIdentifierType type, List<String> ids) {

        assertThat(details.getBusinessIdentifier()).isEqualTo(businessIdentifier);
        assertThat(details.getCategory()).isEqualTo(PersonalDataProcessingCategory.CONSULTATION);
        assertThat(details.getCreatedBy().getIdentifier()).isEqualTo("42");
        assertThat(details.getCreatedBy().getType()).isEqualTo(PdplIdentifierType.OPAL_USER_ID);
        assertThat(details.getCreatedAt()).isEqualTo(OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC));
        assertThat(details.getRecipient()).isNull();
        assertThat(details.getIndividuals()).extracting(ParticipantIdentifier::getIdentifier)
            .containsExactlyElementsOf(ids);
        assertThat(details.getIndividuals())
            .allSatisfy(participant -> assertThat(participant.getType()).isEqualTo(type));
    }

    private static MinorCreditorAccountsSearchResponse response(MinorCreditorAccountSearchResultMinorCreditor... rows) {
        return MinorCreditorAccountsSearchResponse.builder().count(rows.length).creditorAccounts(Arrays.asList(rows))
            .build();
    }

    private static MinorCreditorAccountSearchResultMinorCreditor account(String creditorId, Boolean creditorCompany,
                                                                        String defendantId, Boolean defendantCompany) {

        return MinorCreditorAccountSearchResultMinorCreditor.builder()
            .creditorAccountId(creditorId)
            .organisation(creditorCompany)
            .defendant(MinorCreditorAccountSearchDefendant.builder()
                .defendantAccountId(defendantId).organisation(defendantCompany).build())
            .build();
    }
}
