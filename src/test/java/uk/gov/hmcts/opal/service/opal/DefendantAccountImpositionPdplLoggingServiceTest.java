package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.common.logging.LogUtil;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.GetDefendantAccountImpositionsResponse;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.generated.model.CreditorSummaryCommon;
import uk.gov.hmcts.opal.generated.model.DefendantAccountImpositionCommon;
import uk.gov.hmcts.opal.generated.model.DefendantAccountImpositionsResponseCommon;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

@ExtendWith(MockitoExtension.class)
class DefendantAccountImpositionPdplLoggingServiceTest {

    private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-10-14T10:15:30Z");
    private static final UserState USER_STATE = UserState.builder().userId(42L).build();

    @Mock
    private LoggingService loggingService;

    private DefendantAccountImpositionPdplLoggingService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW.toInstant(), ZoneOffset.UTC);
        service = new DefendantAccountImpositionPdplLoggingService(loggingService, clock);
    }

    @Nested
    class LogIndividualMinorCreditors {

        @Test
        void whenIndividualsArePresent_queuesDeduplicatedConsultationLog_happyPath() {
            GetDefendantAccountImpositionsResponse response = responseWithCreditors(
                creditor(101L, false),
                creditor(101L, false),
                creditor(102L, false),
                creditor(103L, true),
                creditor(104L, null),
                creditor(null, false)
            );
            when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

            try (MockedStatic<LogUtil> logUtil = mockStatic(LogUtil.class)) {
                logUtil.when(LogUtil::getIpAddress).thenReturn("192.0.2.10");
                service.logIndividualMinorCreditors(USER_STATE, response);
            }

            ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
                ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
            verify(loggingService).personalDataAccessLogAsync(captor.capture());
            PersonalDataProcessingLogDetails details = captor.getValue();

            assertAll(
                () -> assertEquals("View Defendant Impositions (Individual Minor Creditor)",
                    details.getBusinessIdentifier()),
                () -> assertEquals(PersonalDataProcessingCategory.CONSULTATION, details.getCategory()),
                () -> assertEquals("42", details.getCreatedBy().getIdentifier()),
                () -> assertEquals(PdplIdentifierType.OPAL_USER_ID, details.getCreatedBy().getType()),
                () -> assertEquals(List.of("101", "102"), details.getIndividuals().stream()
                    .map(ParticipantIdentifier::getIdentifier).toList()),
                () -> assertEquals(List.of(PdplIdentifierType.MINOR_CREDITOR,
                        PdplIdentifierType.MINOR_CREDITOR),
                    details.getIndividuals().stream().map(ParticipantIdentifier::getType).toList()),
                () -> assertEquals("192.0.2.10", details.getIpAddress()),
                () -> assertEquals(NOW, details.getCreatedAt()),
                () -> assertNull(details.getRecipient())
            );
        }

        @ParameterizedTest
        @NullSource
        @MethodSource("responsesWithoutIndividuals")
        void whenNoIndividualMinorCreditorsArePresent_doesNotQueueLog_happyPath(
            GetDefendantAccountImpositionsResponse response) {

            service.logIndividualMinorCreditors(USER_STATE, response);

            verify(loggingService, never()).personalDataAccessLogAsync(any());
        }

        private static Stream<GetDefendantAccountImpositionsResponse> responsesWithoutIndividuals() {
            return Stream.of(
                GetDefendantAccountImpositionsResponse.builder().payload(null).build(),
                GetDefendantAccountImpositionsResponse.builder()
                    .payload(new DefendantAccountImpositionsResponseCommon().impositions(null))
                    .build(),
                responseWithCreditors(),
                responseWithCreditors(creditor(101L, true), creditor(102L, null))
            );
        }
    }

    private static GetDefendantAccountImpositionsResponse responseWithCreditors(CreditorSummaryCommon... creditors) {
        List<DefendantAccountImpositionCommon> impositions = List.of(creditors).stream()
            .map(creditor -> new DefendantAccountImpositionCommon().creditor(creditor))
            .toList();
        return GetDefendantAccountImpositionsResponse.builder()
            .payload(new DefendantAccountImpositionsResponseCommon().impositions(impositions))
            .build();
    }

    private static CreditorSummaryCommon creditor(Long identifier, Boolean organisation) {
        return new CreditorSummaryCommon()
            .creditorAccountId(identifier)
            .minorCreditorOrganisationFlag(organisation);
    }
}
