package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

@ExtendWith(MockitoExtension.class)
class MinorCreditorPdplLoggingServiceTest {

    private static final UserState USER_STATE = UserState.builder().userId(42L).build();

    @Mock
    private LoggingService loggingService;

    private MinorCreditorPdplLoggingService service;

    @BeforeEach
    void setUp() {
        service = new MinorCreditorPdplLoggingService(loggingService, Clock.systemUTC());
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
