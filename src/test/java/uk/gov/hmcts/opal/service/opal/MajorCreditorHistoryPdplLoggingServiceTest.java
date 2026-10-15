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
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.dto.response.GetMajorCreditorHistoryResponse;
import uk.gov.hmcts.opal.generated.model.GetMajorCreditorHistory200Response;
import uk.gov.hmcts.opal.generated.model.MajorCreditorHistoryItemHistory;
import uk.gov.hmcts.opal.generated.model.PostedDetailsCommon;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

@ExtendWith(MockitoExtension.class)
class MajorCreditorHistoryPdplLoggingServiceTest {

    private static final UserState USER_STATE = UserState.builder().userId(42L).build();

    @Mock
    private LoggingService loggingService;

    @Nested
    class LogHistoryItems {

        @Test
        void whenHistoryContainsUsers_queuesConsultationWithUniqueUsers_happyPath() {
            GetMajorCreditorHistoryResponse response = response(
                historyItem("USER-1"),
                historyItem("USER-2"),
                historyItem("USER-1"),
                historyItem(null),
                historyItem(" ")
            );
            ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
                ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
            when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

            service().logHistoryItems(USER_STATE, response);

            verify(loggingService).personalDataAccessLogAsync(captor.capture());
            PersonalDataProcessingLogDetails details = captor.getValue();
            List<ParticipantIdentifier> individuals = details.getIndividuals();

            assertAll(
                () -> assertEquals("View Major Creditor Account History Item", details.getBusinessIdentifier()),
                () -> assertEquals(PersonalDataProcessingCategory.CONSULTATION, details.getCategory()),
                () -> assertEquals(List.of("USER-1", "USER-2"), individuals.stream()
                    .map(ParticipantIdentifier::getIdentifier).toList()),
                () -> assertEquals(List.of(PdplIdentifierType.OPAL_USER_ID, PdplIdentifierType.OPAL_USER_ID),
                    individuals.stream().map(ParticipantIdentifier::getType).toList()),
                () -> assertNull(details.getRecipient())
            );
        }

        @ParameterizedTest
        @NullSource
        @MethodSource("responsesWithoutUsers")
        void whenHistoryContainsNoUsers_doesNotQueueLog_happyPath(GetMajorCreditorHistoryResponse response) {
            service().logHistoryItems(USER_STATE, response);

            verify(loggingService, never()).personalDataAccessLogAsync(any());
        }

        private static Stream<GetMajorCreditorHistoryResponse> responsesWithoutUsers() {
            return Stream.of(
                GetMajorCreditorHistoryResponse.builder().build(),
                GetMajorCreditorHistoryResponse.builder()
                    .payload(new GetMajorCreditorHistory200Response().historyItems(null))
                    .build(),
                response(),
                response(new MajorCreditorHistoryItemHistory()),
                response(historyItem(null)),
                response(historyItem(" "))
            );
        }
    }

    private MajorCreditorHistoryPdplLoggingService service() {
        return new MajorCreditorHistoryPdplLoggingService(loggingService, Clock.systemUTC());
    }

    private static GetMajorCreditorHistoryResponse response(MajorCreditorHistoryItemHistory... items) {
        return GetMajorCreditorHistoryResponse.builder()
            .payload(new GetMajorCreditorHistory200Response().historyItems(List.of(items)))
            .build();
    }

    private static MajorCreditorHistoryItemHistory historyItem(String postedBy) {
        return new MajorCreditorHistoryItemHistory()
            .postedDetails(new PostedDetailsCommon().postedBy(postedBy));
    }
}
