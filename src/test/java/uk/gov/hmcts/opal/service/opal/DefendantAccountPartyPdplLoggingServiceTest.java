package uk.gov.hmcts.opal.service.opal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import uk.gov.hmcts.opal.common.logging.LogUtil;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.entity.defendantaccount.AssociationType;
import uk.gov.hmcts.opal.exception.PdplLoggingException;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

class DefendantAccountPartyPdplLoggingServiceTest {

    @ParameterizedTest
    @CsvSource({
        "DEFENDANT, false, false, Update Defendant, ALTERATION, DEFENDANT",
        "DEFENDANT, true, true, Update Defendant, ALTERATION, DEFENDANT",
        "PARENT_GUARDIAN, false, false, Update Parent/Guardian, ALTERATION, PARENT_GUARDIAN",
        "DEFENDANT, true, false, Convert Company to Individual, COLLECTION, DEFENDANT",
        "DEFENDANT, false, true, Convert Individual to Company, ERASURE, DEFENDANT"
    })
    void logReplacement_queuesOperationDetails(AssociationType associationType, boolean wasOrganisation,
        boolean organisation, String businessIdentifier, PersonalDataProcessingCategory category,
        PdplIdentifierType identifierType) {
        LoggingService loggingService = mock(LoggingService.class);
        OffsetDateTime now = OffsetDateTime.parse("2026-10-06T10:15:30Z");
        DefendantAccountPartyPdplLoggingService service = new DefendantAccountPartyPdplLoggingService(
            loggingService, Clock.fixed(now.toInstant(), ZoneOffset.UTC));
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        try (MockedStatic<LogUtil> logUtil = mockStatic(LogUtil.class)) {
            logUtil.when(LogUtil::getIpAddress).thenReturn("192.0.2.1");
            service.logReplacement(98765L, associationType, wasOrganisation, organisation, List.of(),
                UserState.builder().userId(123L).build());
        }

        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
        verify(loggingService).personalDataAccessLogAsync(captor.capture());
        PersonalDataProcessingLogDetails details = captor.getValue();
        assertThat(details.getBusinessIdentifier()).isEqualTo(businessIdentifier);
        assertThat(details.getCategory()).isEqualTo(category);
        assertThat(details.getIndividuals()).hasSize(1);
        assertThat(details.getIndividuals().getFirst().getIdentifier()).isEqualTo("98765");
        assertThat(details.getIndividuals().getFirst().getType()).isEqualTo(identifierType);
        assertThat(details.getCreatedBy().getIdentifier()).isEqualTo("123");
        assertThat(details.getCreatedBy().getType()).isEqualTo(PdplIdentifierType.OPAL_USER_ID);
        assertThat(details.getIpAddress()).isEqualTo("192.0.2.1");
        assertThat(details.getCreatedAt()).isEqualTo(now);
        assertThat(details.getRecipient()).isNull();
    }

    @Test
    void logReplacement_removedGuardians_areLoggedWithTheirPartyIds() {
        LoggingService loggingService = mock(LoggingService.class);
        DefendantAccountPartyPdplLoggingService service =
            new DefendantAccountPartyPdplLoggingService(loggingService, Clock.systemUTC());
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        try (MockedStatic<LogUtil> ignored = mockStatic(LogUtil.class)) {
            service.logReplacement(98765L, AssociationType.DEFENDANT, false, true,
                List.of(101L, 102L, 101L), UserState.builder().userId(123L).build());
        }

        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
        verify(loggingService, times(2)).personalDataAccessLogAsync(captor.capture());
        PersonalDataProcessingLogDetails details = captor.getAllValues().get(1);
        assertThat(details.getBusinessIdentifier()).isEqualTo("Remove Parent/Guardian");
        assertThat(details.getCategory()).isEqualTo(PersonalDataProcessingCategory.ERASURE);
        assertThat(details.getIndividuals()).extracting("identifier").containsExactly("101", "102");
        assertThat(details.getIndividuals()).extracting("type").containsOnly(PdplIdentifierType.PARENT_GUARDIAN);
    }

    @ParameterizedTest
    @CsvSource({"false, false", "false, true", "true, false", "true, true"})
    void logReplacement_queueFailure_throws(boolean failRemoval, boolean raisesError) {
        LoggingService loggingService = mock(LoggingService.class);
        DefendantAccountPartyPdplLoggingService service =
            new DefendantAccountPartyPdplLoggingService(loggingService, Clock.systemUTC());
        when(loggingService.personalDataAccessLogAsync(any())).thenAnswer(invocation -> {
            PersonalDataProcessingLogDetails details = invocation.getArgument(0);
            boolean removal = "Remove Parent/Guardian".equals(details.getBusinessIdentifier());
            if (removal == failRemoval) {
                if (raisesError) {
                    throw new IllegalStateException("Queue unavailable");
                }
                return false;
            }
            return true;
        });

        try (MockedStatic<LogUtil> ignored = mockStatic(LogUtil.class)) {
            assertThrows(PdplLoggingException.class, () -> service.logReplacement(
                98765L, AssociationType.DEFENDANT, false, true, List.of(101L),
                UserState.builder().userId(123L).build()));
        }
        verify(loggingService, times(failRemoval ? 2 : 1)).personalDataAccessLogAsync(any());
    }

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
