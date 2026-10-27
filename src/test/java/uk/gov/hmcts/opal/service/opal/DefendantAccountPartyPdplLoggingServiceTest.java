package uk.gov.hmcts.opal.service.opal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import uk.gov.hmcts.opal.common.logging.LogUtil;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.exception.PdplLoggingException;
import uk.gov.hmcts.opal.generated.model.RemoveDefendantAccountPartyDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.RemoveDefendantAccountPartyRequestDefendantAccount;
import uk.gov.hmcts.opal.generated.model.RemoveDefendantAccountPartyResponseDefendantAccount;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

class DefendantAccountPartyPdplLoggingServiceTest {

    private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-10-06T10:15:30Z");
    private static final Clock CLOCK = Clock.fixed(NOW.toInstant(), ZoneOffset.UTC);

    @Nested
    class LogAddedParentGuardian {

        @Test
        void whenQueueAccepts_queuesExactPdpoDetails_happyPath() {
            LoggingService loggingService = mock(LoggingService.class);
            UserState userState = mock(UserState.class);
            DefendantAccountPartyPdplLoggingService service =
                new DefendantAccountPartyPdplLoggingService(loggingService, CLOCK);
            when(userState.getUserId()).thenReturn(123L);
            when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

            boolean queued;
            try (MockedStatic<LogUtil> logUtil = mockStatic(LogUtil.class)) {
                logUtil.when(LogUtil::getIpAddress).thenReturn("192.0.2.1");
                queued = service.logAddedParentGuardian("98765", userState);
            }

            PersonalDataProcessingLogDetails details = captureLogDetails(loggingService);
            assertAll(
                () -> assertThat(queued).isTrue(),
                () -> assertThat(details.getBusinessIdentifier()).isEqualTo("Add Parent/Guardian"),
                () -> assertThat(details.getCategory()).isEqualTo(PersonalDataProcessingCategory.COLLECTION),
                () -> assertCommonDetails(details)
            );
        }

        @Test
        void whenQueueRejects_returnsFalse_sadPath() {
            LoggingService loggingService = mock(LoggingService.class);
            UserState userState = mock(UserState.class);
            DefendantAccountPartyPdplLoggingService service =
                new DefendantAccountPartyPdplLoggingService(loggingService, CLOCK);
            when(userState.getUserId()).thenReturn(123L);
            when(loggingService.personalDataAccessLogAsync(any())).thenReturn(false);

            boolean queued;
            try (MockedStatic<LogUtil> logUtil = mockStatic(LogUtil.class)) {
                logUtil.when(LogUtil::getIpAddress).thenReturn("192.0.2.1");
                queued = service.logAddedParentGuardian("98765", userState);
            }

            assertThat(queued).isFalse();
            verify(loggingService).personalDataAccessLogAsync(any());
        }
    }

    @Nested
    class LogRemovedParentGuardian {

        @Test
        void whenQueueAccepts_queuesExactPdpoDetails_happyPath() {
            LoggingService loggingService = mock(LoggingService.class);
            UserState userState = mock(UserState.class);
            DefendantAccountPartyPdplLoggingService service =
                new DefendantAccountPartyPdplLoggingService(loggingService, CLOCK);
            when(userState.getUserId()).thenReturn(123L);
            when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

            try (MockedStatic<LogUtil> logUtil = mockStatic(LogUtil.class)) {
                logUtil.when(LogUtil::getIpAddress).thenReturn("192.0.2.1");
                service.logRemovedParentGuardian(999L, requestWithPartyId(), successfulResponse(), userState);
            }

            PersonalDataProcessingLogDetails details = captureLogDetails(loggingService);
            assertAll(
                () -> assertThat(details.getBusinessIdentifier()).isEqualTo("Remove Parent/Guardian"),
                () -> assertThat(details.getCategory()).isEqualTo(PersonalDataProcessingCategory.ERASURE),
                () -> assertCommonDetails(details)
            );
        }

        @Test
        void whenQueueRejects_throwsPdplLoggingException_sadPath() {
            LoggingService loggingService = mock(LoggingService.class);
            UserState userState = mock(UserState.class);
            DefendantAccountPartyPdplLoggingService service =
                new DefendantAccountPartyPdplLoggingService(loggingService, CLOCK);
            when(userState.getUserId()).thenReturn(123L);
            when(loggingService.personalDataAccessLogAsync(any())).thenReturn(false);

            PdplLoggingException exception = assertThrows(PdplLoggingException.class,
                () -> service.logRemovedParentGuardian(
                    999L, requestWithPartyId(), successfulResponse(), userState));

            assertAll(
                () -> assertThat(exception.getMessage())
                    .isEqualTo("Unable to queue PDPO log for removed Parent/Guardian"),
                () -> verify(loggingService).personalDataAccessLogAsync(any())
            );
        }

        @Test
        void whenRemovalFails_doesNotQueuePdpoLog() {
            LoggingService loggingService = mock(LoggingService.class);
            UserState userState = mock(UserState.class);
            DefendantAccountPartyPdplLoggingService service =
                new DefendantAccountPartyPdplLoggingService(loggingService, CLOCK);

            service.logRemovedParentGuardian(
                999L, requestWithPartyId(), new RemoveDefendantAccountPartyResponseDefendantAccount(), userState);

            verifyNoInteractions(loggingService, userState);
        }

        @Test
        void whenRequestHasNoPartyDetails_usesPathPartyIdentifier() {
            LoggingService loggingService = mock(LoggingService.class);
            UserState userState = mock(UserState.class);
            DefendantAccountPartyPdplLoggingService service =
                new DefendantAccountPartyPdplLoggingService(loggingService, CLOCK);
            when(userState.getUserId()).thenReturn(123L);
            when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);
            RemoveDefendantAccountPartyRequestDefendantAccount request =
                RemoveDefendantAccountPartyRequestDefendantAccount.builder()
                    .defendantAccountPartyId("legacy-party-reference")
                    .build();

            service.logRemovedParentGuardian(98765L, request, successfulResponse(), userState);

            assertThat(captureLogDetails(loggingService).getIndividuals().getFirst().getIdentifier())
                .isEqualTo("98765");
        }
    }

    private PersonalDataProcessingLogDetails captureLogDetails(LoggingService loggingService) {
        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
        verify(loggingService).personalDataAccessLogAsync(captor.capture());
        return captor.getValue();
    }

    private void assertCommonDetails(PersonalDataProcessingLogDetails details) {
        assertAll(
            () -> assertThat(details.getCreatedBy().getIdentifier()).isEqualTo("123"),
            () -> assertThat(details.getCreatedBy().getType()).isEqualTo(PdplIdentifierType.OPAL_USER_ID),
            () -> assertThat(details.getIndividuals()).hasSize(1),
            () -> assertThat(details.getIndividuals().getFirst().getIdentifier()).isEqualTo("98765"),
            () -> assertThat(details.getIndividuals().getFirst().getType())
                .isEqualTo(PdplIdentifierType.PARENT_GUARDIAN),
            () -> assertThat(details.getIpAddress()).isEqualTo("192.0.2.1"),
            () -> assertThat(details.getCreatedAt()).isEqualTo(NOW),
            () -> assertThat(details.getRecipient()).isNull()
        );
    }

    private RemoveDefendantAccountPartyRequestDefendantAccount requestWithPartyId() {
        return RemoveDefendantAccountPartyRequestDefendantAccount.builder()
            .partyDetails(RemoveDefendantAccountPartyDetailsCommonStrict.builder()
                .partyId("98765")
                .build())
            .build();
    }

    private RemoveDefendantAccountPartyResponseDefendantAccount successfulResponse() {
        return RemoveDefendantAccountPartyResponseDefendantAccount.builder()
            .defendantAccountPartyId("999")
            .build();
    }
}
