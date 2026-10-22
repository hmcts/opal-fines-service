package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.common.logging.LogUtil;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.exception.PdplLoggingException;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

@ExtendWith(MockitoExtension.class)
class MinorCreditorPdplLoggingServiceTest {

    private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-10-21T10:15:30Z");
    private static final UserState USER_STATE = UserState.builder().userId(42L).build();

    @Mock
    private LoggingService loggingService;

    private MinorCreditorPdplLoggingService service;

    @BeforeEach
    void setUp() {
        service = new MinorCreditorPdplLoggingService(loggingService, Clock.fixed(NOW.toInstant(), ZoneOffset.UTC));
    }

    @Test
    void logUpdate_queuesExpectedAlterationDetails() {
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        try (MockedStatic<LogUtil> logUtil = mockStatic(LogUtil.class)) {
            logUtil.when(LogUtil::getIpAddress).thenReturn("192.0.2.10");
            service.logUpdate(607L, USER_STATE);
        }

        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
        verify(loggingService).personalDataAccessLogAsync(captor.capture());
        PersonalDataProcessingLogDetails details = captor.getValue();
        assertAll(
            () -> assertEquals("Update Minor Creditor", details.getBusinessIdentifier()),
            () -> assertEquals(PersonalDataProcessingCategory.ALTERATION, details.getCategory()),
            () -> assertEquals("42", details.getCreatedBy().getIdentifier()),
            () -> assertEquals(PdplIdentifierType.OPAL_USER_ID, details.getCreatedBy().getType()),
            () -> assertEquals(1, details.getIndividuals().size()),
            () -> assertEquals("607", details.getIndividuals().getFirst().getIdentifier()),
            () -> assertEquals(PdplIdentifierType.MINOR_CREDITOR, details.getIndividuals().getFirst().getType()),
            () -> assertEquals("192.0.2.10", details.getIpAddress()),
            () -> assertEquals(NOW, details.getCreatedAt()),
            () -> assertNull(details.getRecipient())
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void logUpdate_whenQueueRejectsOrThrows_propagatesLoggingFailure(boolean raisesError) {
        when(loggingService.personalDataAccessLogAsync(any())).thenAnswer(invocation -> {
            if (raisesError) {
                throw new IllegalStateException("Queue unavailable");
            }
            return false;
        });

        assertThrows(PdplLoggingException.class, () -> service.logUpdate(607L, USER_STATE));
        verify(loggingService).personalDataAccessLogAsync(any());
    }

    @Nested
    class LogIndividualMinorCreditor {

        @Test
        void whenMinorCreditorIsIndividual_queuesConsultationLog_happyPath() {
            MinorCreditorAccountResponse response = response(607L, false);
            when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

            service.logIndividualMinorCreditor(USER_STATE, response);

            ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
                ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
            verify(loggingService).personalDataAccessLogAsync(captor.capture());
            PersonalDataProcessingLogDetails details = captor.getValue();
            List<ParticipantIdentifier> individuals = details.getIndividuals();

            assertAll(
                () -> assertEquals("View Minor Creditor", details.getBusinessIdentifier()),
                () -> assertEquals(PersonalDataProcessingCategory.CONSULTATION, details.getCategory()),
                () -> assertEquals("42", details.getCreatedBy().getIdentifier()),
                () -> assertEquals(PdplIdentifierType.OPAL_USER_ID, details.getCreatedBy().getType()),
                () -> assertEquals(List.of("607"), individuals.stream()
                    .map(ParticipantIdentifier::getIdentifier).toList()),
                () -> assertEquals(List.of(PdplIdentifierType.MINOR_CREDITOR), individuals.stream()
                    .map(ParticipantIdentifier::getType).toList()),
                () -> assertNull(details.getRecipient())
            );
        }

        @ParameterizedTest
        @NullSource
        @MethodSource("responsesNotRequiringLog")
        void whenMinorCreditorIsNotAnIndividual_doesNotQueueLog_happyPath(MinorCreditorAccountResponse response) {
            service.logIndividualMinorCreditor(USER_STATE, response);

            verify(loggingService, never()).personalDataAccessLogAsync(any());
        }

        private static Stream<MinorCreditorAccountResponse> responsesNotRequiringLog() {
            MinorCreditorAccountResponse withoutParty = new MinorCreditorAccountResponse();
            withoutParty.setCreditorAccountId(607L);
            return Stream.of(withoutParty, response(608L, true), response(609L, null));
        }
    }

    private static MinorCreditorAccountResponse response(Long accountId, Boolean organisationFlag) {
        MinorCreditorAccountResponse response = new MinorCreditorAccountResponse();
        response.setCreditorAccountId(accountId);
        response.setPartyDetails(new PartyDetailsCommon().organisationFlag(organisationFlag));
        return response;
    }
}
