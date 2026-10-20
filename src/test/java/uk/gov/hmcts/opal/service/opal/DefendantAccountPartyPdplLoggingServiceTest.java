package uk.gov.hmcts.opal.service.opal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.common.logging.LogUtil;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.entity.defendantaccount.AssociationType;
import uk.gov.hmcts.opal.exception.PdplLoggingException;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

@ExtendWith(MockitoExtension.class)
class DefendantAccountPartyPdplLoggingServiceTest {

    private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-10-06T10:15:30Z");
    private static final UserState USER_STATE = UserState.builder().userId(123L).build();

    @Mock
    private LoggingService loggingService;

    private DefendantAccountPartyPdplLoggingService service;

    @BeforeEach
    void setUp() {
        service = new DefendantAccountPartyPdplLoggingService(
            loggingService, Clock.fixed(NOW.toInstant(), ZoneOffset.UTC));
    }

    @ParameterizedTest(name = "{0}: organisation {1} → {2}, {3}")
    @CsvSource({
        "DEFENDANT, false, false, Update Defendant, ALTERATION, DEFENDANT_ACCOUNT",
        "DEFENDANT, true, true, Update Defendant, ALTERATION, DEFENDANT_ACCOUNT",
        "PARENT_GUARDIAN, false, false, Update Parent/Guardian, ALTERATION, PARENT_GUARDIAN",
        "DEFENDANT, true, false, Convert Company to Individual, COLLECTION, DEFENDANT_ACCOUNT",
        "DEFENDANT, false, true, Convert Individual to Company, ERASURE, DEFENDANT_ACCOUNT"
    })
    void logReplacement_whenSuccessful_queuesExpectedOperationDetails(AssociationType associationType,
        boolean wasOrganisation,
        boolean organisation, String businessIdentifier, PersonalDataProcessingCategory category,
        PdplIdentifierType identifierType) {
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        try (MockedStatic<LogUtil> logUtil = mockStatic(LogUtil.class)) {
            logUtil.when(LogUtil::getIpAddress).thenReturn("192.0.2.1");
            service.logReplacement(98765L, associationType, wasOrganisation, organisation, List.of(),
                USER_STATE);
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
        assertThat(details.getCreatedAt()).isEqualTo(NOW);
        assertThat(details.getRecipient()).isNull();
    }

    @Test
    void logReplacement_whenGuardiansAreRemoved_queuesDistinctPartyIds() {
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        try (MockedStatic<LogUtil> ignored = mockStatic(LogUtil.class)) {
            service.logReplacement(98765L, AssociationType.DEFENDANT, false, true,
                List.of(101L, 102L, 101L), USER_STATE);
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

    @ParameterizedTest(name = "{0}")
    @CsvSource({
        "Conversion log rejected, false, false", "Conversion log throws, false, true",
        "Removal log rejected, true, false", "Removal log throws, true, true"
    })
    void logReplacement_whenQueueFails_throwsPdplLoggingException(String scenario,
        boolean failRemoval, boolean raisesError) {
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
                USER_STATE));
        }
        verify(loggingService, times(failRemoval ? 2 : 1)).personalDataAccessLogAsync(any());
    }

    @Test
    void logAddedParentGuardian_queuesExactPdpoDetails() {
        // Arrange
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        // Act
        boolean queued;
        try (MockedStatic<LogUtil> logUtil = mockStatic(LogUtil.class)) {
            logUtil.when(LogUtil::getIpAddress).thenReturn("192.0.2.1");
            queued = service.logAddedParentGuardian("98765", USER_STATE);
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
        assertThat(details.getCreatedAt()).isEqualTo(NOW);
        assertThat(details.getRecipient()).isNull();
    }

    @Test
    void logAddedParentGuardian_whenQueueRejects_returnsFalse() {
        // Arrange
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(false);

        // Act
        boolean queued;
        try (MockedStatic<LogUtil> logUtil = mockStatic(LogUtil.class)) {
            logUtil.when(LogUtil::getIpAddress).thenReturn("192.0.2.1");
            queued = service.logAddedParentGuardian("98765", USER_STATE);
        }

        // Assert
        assertThat(queued).isFalse();
        verify(loggingService).personalDataAccessLogAsync(any());
    }
}
