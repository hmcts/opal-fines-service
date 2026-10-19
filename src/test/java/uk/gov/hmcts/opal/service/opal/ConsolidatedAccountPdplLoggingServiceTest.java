package uk.gov.hmcts.opal.service.opal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.common.logging.LogUtil;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.exception.PdplLoggingException;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

@ExtendWith(MockitoExtension.class)
class ConsolidatedAccountPdplLoggingServiceTest {

    private static final OffsetDateTime DATE_TIME = OffsetDateTime.parse("2026-10-19T10:15:30Z");
    private static final UserState USER_STATE = UserState.builder().userId(42L).build();

    private ConsolidatedAccountPdplLoggingService service;

    @Mock
    private LoggingService loggingService;

    @BeforeEach
    void setUp() {
        service = new ConsolidatedAccountPdplLoggingService(loggingService,
            Clock.fixed(DATE_TIME.toInstant(), ZoneOffset.UTC));
    }

    @Test
    void logIndividualChildAccounts_queuesOneLogWithDistinctDefendants() {
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        try (MockedStatic<LogUtil> logUtil = mockStatic(LogUtil.class)) {
            logUtil.when(LogUtil::getIpAddress).thenReturn("192.0.2.1");
            service.logIndividualChildAccounts(USER_STATE, List.of(101L, 102L, 101L));
        }

        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
        verify(loggingService).personalDataAccessLogAsync(captor.capture());
        PersonalDataProcessingLogDetails details = captor.getValue();

        assertThat(details.getBusinessIdentifier()).isEqualTo("View Consolidated Accounts - Individual");
        assertThat(details.getCategory()).isEqualTo(PersonalDataProcessingCategory.CONSULTATION);
        assertThat(details.getIndividuals())
            .extracting(ParticipantIdentifier::getIdentifier)
            .containsExactly("101", "102");
        assertThat(details.getIndividuals())
            .extracting(ParticipantIdentifier::getType)
            .containsExactly(PdplIdentifierType.DEFENDANT, PdplIdentifierType.DEFENDANT);
        assertThat(details.getCreatedBy().getIdentifier()).isEqualTo("42");
        assertThat(details.getCreatedBy().getType()).isEqualTo(PdplIdentifierType.OPAL_USER_ID);
        assertThat(details.getIpAddress()).isEqualTo("192.0.2.1");
        assertThat(details.getCreatedAt()).isEqualTo(DATE_TIME);
        assertThat(details.getRecipient()).isNull();
    }

    @Test
    void logIndividualChildAccounts_withNoIndividuals_doesNotQueue() {
        service.logIndividualChildAccounts(USER_STATE, List.of());

        verifyNoInteractions(loggingService);
    }

    @Test
    void logIndividualChildAccounts_whenQueueRejects_throwsPdplLoggingException() {
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(false);

        PdplLoggingException exception = assertThrows(PdplLoggingException.class,
            () -> service.logIndividualChildAccounts(USER_STATE, List.of(101L)));

        assertThat(exception).hasMessage("Unable to queue PDPO log for consolidated accounts");
        verify(loggingService).personalDataAccessLogAsync(any());
    }

    @Test
    void logIndividualChildAccounts_whenQueueThrows_wrapsPdplLoggingException() {
        RuntimeException queueFailure = new RuntimeException("queue unavailable");
        when(loggingService.personalDataAccessLogAsync(any())).thenThrow(queueFailure);

        PdplLoggingException exception = assertThrows(PdplLoggingException.class,
            () -> service.logIndividualChildAccounts(USER_STATE, List.of(101L)));

        assertThat(exception).hasMessage("Unable to queue PDPO log for consolidated accounts")
            .hasCause(queueFailure);
        verify(loggingService).personalDataAccessLogAsync(any());
    }
}
