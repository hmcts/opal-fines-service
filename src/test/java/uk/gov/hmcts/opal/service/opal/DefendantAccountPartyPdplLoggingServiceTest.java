package uk.gov.hmcts.opal.service.opal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import uk.gov.hmcts.opal.common.logging.LogUtil;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

class DefendantAccountPartyPdplLoggingServiceTest {

    @Test
    void logAddedParentGuardian_queuesExactPdpoDetails() {
        // Arrange
        LoggingService loggingService = mock(LoggingService.class);
        UserState userState = mock(UserState.class);
        OffsetDateTime now = OffsetDateTime.parse("2026-10-06T10:15:30Z");
        Clock clock = Clock.fixed(now.toInstant(), ZoneOffset.UTC);
        DefendantAccountPartyPdplLoggingService service =
            new DefendantAccountPartyPdplLoggingService(loggingService, clock);
        when(userState.getUserId()).thenReturn(123L);
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        // Act
        boolean queued;
        try (MockedStatic<LogUtil> logUtil = mockStatic(LogUtil.class)) {
            logUtil.when(LogUtil::getIpAddress).thenReturn("192.0.2.1");
            queued = service.logAddedParentGuardian("98765", userState);
        }

        // Assert
        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
        verify(loggingService).personalDataAccessLogAsync(captor.capture());
        PersonalDataProcessingLogDetails details = captor.getValue();

        assertThat(queued).isTrue();
        assertThat(details.getBusinessIdentifier()).isEqualTo("Add Parent/Guardian");
        assertThat(details.getCategory()).isEqualTo(PersonalDataProcessingCategory.COLLECTION);
        assertThat(details.getCreatedBy().getIdentifier()).isEqualTo("123");
        assertThat(details.getCreatedBy().getType()).isEqualTo(PdplIdentifierType.OPAL_USER_ID);
        assertThat(details.getIndividuals()).hasSize(1);
        assertThat(details.getIndividuals().getFirst().getIdentifier()).isEqualTo("98765");
        assertThat(details.getIndividuals().getFirst().getType()).isEqualTo(PdplIdentifierType.PARENT_GUARDIAN);
        assertThat(details.getIpAddress()).isEqualTo("192.0.2.1");
        assertThat(details.getCreatedAt()).isEqualTo(now);
        assertThat(details.getRecipient()).isNull();
    }

    @Test
    void logAddedParentGuardian_whenQueueRejects_returnsFalse() {
        // Arrange
        LoggingService loggingService = mock(LoggingService.class);
        UserState userState = mock(UserState.class);
        Clock clock = Clock.fixed(OffsetDateTime.parse("2026-10-06T10:15:30Z").toInstant(), ZoneOffset.UTC);
        DefendantAccountPartyPdplLoggingService service =
            new DefendantAccountPartyPdplLoggingService(loggingService, clock);
        when(userState.getUserId()).thenReturn(123L);
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(false);

        // Act
        boolean queued;
        try (MockedStatic<LogUtil> logUtil = mockStatic(LogUtil.class)) {
            logUtil.when(LogUtil::getIpAddress).thenReturn("192.0.2.1");
            queued = service.logAddedParentGuardian("98765", userState);
        }

        // Assert
        assertThat(queued).isFalse();
        verify(loggingService).personalDataAccessLogAsync(any());
    }
}
