package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.common.logging.LogUtil;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

@ExtendWith(MockitoExtension.class)
@DisplayName("PO-2576 PDPO logging tests")
class InterfaceJobProcessedFileSummaryPdplLoggingServiceTest {

    @Mock
    private LoggingService loggingService;
    @Mock
    private UserStateV2 userState;
    private static final Long USER_ID = 42L;

    @Test
    @DisplayName("PO-2576 - maps the payer consultation log")
    void logsConsultationWithPayerAndUserDetails() {
        // Arrange
        OffsetDateTime now = OffsetDateTime.parse("2026-08-24T10:15:30Z");
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);
        when(userState.getUserId()).thenReturn(USER_ID);

        InterfaceJobProcessedFileSummaryPdplLoggingService service =
            new InterfaceJobProcessedFileSummaryPdplLoggingService(
                loggingService, Clock.fixed(now.toInstant(), ZoneOffset.UTC));

        try (MockedStatic<LogUtil> logUtil = Mockito.mockStatic(LogUtil.class)) {
            logUtil.when(LogUtil::getIpAddress).thenReturn("192.0.2.10");

            // Act
            service.logView(userState);
        }

        // Assert
        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
        verify(loggingService).personalDataAccessLogAsync(captor.capture());
        PersonalDataProcessingLogDetails details = captor.getValue();

        assertEquals("View File Processing Summary", details.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.CONSULTATION, details.getCategory());
        assertEquals("192.0.2.10", details.getIpAddress());
        assertEquals(now, details.getCreatedAt());
        assertEquals("42", details.getCreatedBy().getIdentifier());
        assertEquals(PdplIdentifierType.OPAL_USER_ID, details.getCreatedBy().getType());
        ParticipantIdentifier payer = details.getIndividuals().getFirst();
        assertEquals(PdplIdentifierType.PAYER, payer.getType());
        assertNull(payer.getIdentifier());
        assertNull(details.getRecipient());
    }

    @Test
    @DisplayName("PO-2576 - isolates logging integration failures")
    void containsLoggingIntegrationFailures() {
        // Arrange
        when(userState.getUserId()).thenReturn(USER_ID);
        doThrow(new RuntimeException("logging unavailable"))
            .when(loggingService).personalDataAccessLogAsync(any());
        InterfaceJobProcessedFileSummaryPdplLoggingService service =
            new InterfaceJobProcessedFileSummaryPdplLoggingService(loggingService, Clock.systemUTC());

        // Act
        service.logView(userState);

        // Assert
        verify(loggingService).personalDataAccessLogAsync(any());
    }

    @Test
    @DisplayName("PO-2576 - handles a library retry exhaustion result")
    void containsLoggingRetryExhaustion() {
        // Arrange
        when(userState.getUserId()).thenReturn(USER_ID);
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(false);
        InterfaceJobProcessedFileSummaryPdplLoggingService service =
            new InterfaceJobProcessedFileSummaryPdplLoggingService(loggingService, Clock.systemUTC());

        // Act
        service.logView(userState);

        // Assert
        verify(loggingService).personalDataAccessLogAsync(any());
    }
}
