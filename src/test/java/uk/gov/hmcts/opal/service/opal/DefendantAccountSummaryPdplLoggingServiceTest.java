package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.common.logging.LogUtil;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.GetDefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.exception.PdplLoggingException;
import uk.gov.hmcts.opal.generated.model.AtAGlanceResponseDefendantAccount;
import uk.gov.hmcts.opal.generated.model.GetDefendantAccountHeaderSummary200Response;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommonStrict;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

@ExtendWith(MockitoExtension.class)
class DefendantAccountSummaryPdplLoggingServiceTest {
    private static final long USER_ID = 123L;
    private static final String IP_ADDRESS = "192.0.2.1";
    private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-10-06T10:15:30Z");
    private static final String FAILURE_MESSAGE = "Unable to queue PDPO log for viewing a defendant account summary";

    @Mock
    private LoggingService loggingService;

    @Mock
    private Clock clock;

    @InjectMocks
    private DefendantAccountSummaryPdplLoggingService pdplLoggingService;

    @Test
    void testLogAtAGlance_defendant_success() {
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(NOW.toInstant());

        try (MockedStatic<LogUtil> logUtil = mockStatic(LogUtil.class)) {
            logUtil.when(LogUtil::getIpAddress).thenReturn(IP_ADDRESS);
            pdplLoggingService.logAtAGlance(
                atAGlanceResponse(AtAGlanceResponseDefendantAccount.DebtorTypeEnum.DEFENDANT), USER_ID);
        }

        assertPdplDetails("12345", PdplIdentifierType.DEFENDANT_ACCOUNT);
    }

    @Test
    void testLogAtAGlance_defendant_logPdplFailure() {
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(false);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(NOW.toInstant());

        PdplLoggingException exception;
        try (MockedStatic<LogUtil> logUtil = mockStatic(LogUtil.class)) {
            logUtil.when(LogUtil::getIpAddress).thenReturn(IP_ADDRESS);
            exception = assertThrows(PdplLoggingException.class,
                () -> pdplLoggingService.logAtAGlance(
                    atAGlanceResponse(AtAGlanceResponseDefendantAccount.DebtorTypeEnum.DEFENDANT), USER_ID));
        }

        assertEquals(FAILURE_MESSAGE, exception.getMessage());
        assertNull(exception.getCause());
        verify(loggingService).personalDataAccessLogAsync(any());
    }

    @Test
    void testLogHeaderSummary_parentGuardian_success() {
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(NOW.toInstant());

        try (MockedStatic<LogUtil> logUtil = mockStatic(LogUtil.class)) {
            logUtil.when(LogUtil::getIpAddress).thenReturn(IP_ADDRESS);
            pdplLoggingService.logHeaderSummary(
                headerSummary(GetDefendantAccountHeaderSummary200Response.DebtorTypeEnum.PARENT_GUARDIAN), USER_ID);
        }

        assertPdplDetails("67890", PdplIdentifierType.PARENT_GUARDIAN);
    }

    @Test
    void testLogHeaderSummary_parentGuardian_logPdplFailure() {
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(false);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(NOW.toInstant());

        PdplLoggingException exception;
        try (MockedStatic<LogUtil> logUtil = mockStatic(LogUtil.class)) {
            logUtil.when(LogUtil::getIpAddress).thenReturn(IP_ADDRESS);
            exception = assertThrows(PdplLoggingException.class,
                () -> pdplLoggingService.logHeaderSummary(headerSummary(
                    GetDefendantAccountHeaderSummary200Response.DebtorTypeEnum.PARENT_GUARDIAN), USER_ID));
        }

        assertEquals(FAILURE_MESSAGE, exception.getMessage());
        assertNull(exception.getCause());
        verify(loggingService).personalDataAccessLogAsync(any());
    }

    @Test
    void testLogAtAGlance_null_noLoggingSuccess() {
        GetDefendantAccountAtAGlanceResponse response = GetDefendantAccountAtAGlanceResponse.builder()
            .payload(AtAGlanceResponseDefendantAccount.builder().defendantAccountId("12345").build())
            .build();

        assertDoesNotThrow(() -> pdplLoggingService.logAtAGlance(response, USER_ID));

        verifyNoInteractions(loggingService);
    }

    @Test
    void testLogHeaderSummary_null_noLoggingSuccess() {
        DefendantAccountHeaderSummary response = DefendantAccountHeaderSummary.builder()
            .response(GetDefendantAccountHeaderSummary200Response.builder().defendantAccountId("12345").build())
            .build();

        assertDoesNotThrow(() -> pdplLoggingService.logHeaderSummary(response, USER_ID));

        verifyNoInteractions(loggingService);
    }

    @Test
    void testLogAtAGlance_company_noLoggingSuccess() {
        GetDefendantAccountAtAGlanceResponse response = GetDefendantAccountAtAGlanceResponse.builder()
            .payload(AtAGlanceResponseDefendantAccount.builder().defendantAccountId("12345")
                .partyDetails(PartyDetailsCommonStrict.builder().organisationFlag(true).build())
                .build())
            .build();

        assertDoesNotThrow(() -> pdplLoggingService.logAtAGlance(response, USER_ID));

        verifyNoInteractions(loggingService);
    }

    @Test
    void testLogHeaderSummary_company_noLoggingSuccess() {
        DefendantAccountHeaderSummary response = DefendantAccountHeaderSummary.builder()
            .response(GetDefendantAccountHeaderSummary200Response.builder().defendantAccountId("12345")
                .partyDetails(PartyDetailsCommon.builder().organisationFlag(true).build())
                .build())
            .build();

        assertDoesNotThrow(() -> pdplLoggingService.logHeaderSummary(response, USER_ID));

        verifyNoInteractions(loggingService);
    }


    private GetDefendantAccountAtAGlanceResponse atAGlanceResponse(
        AtAGlanceResponseDefendantAccount.DebtorTypeEnum debtorType) {

        return GetDefendantAccountAtAGlanceResponse.builder()
            .payload(AtAGlanceResponseDefendantAccount.builder()
                .defendantAccountId("12345")
                .debtorType(debtorType)
                .partyDetails(PartyDetailsCommonStrict.builder().organisationFlag(false).build())
                .build())
            .build();
    }

    private DefendantAccountHeaderSummary headerSummary(
        GetDefendantAccountHeaderSummary200Response.DebtorTypeEnum debtorType) {

        return DefendantAccountHeaderSummary.builder()
            .response(GetDefendantAccountHeaderSummary200Response.builder()
                .defendantAccountId("67890")
                .debtorType(debtorType)
                .partyDetails(PartyDetailsCommon.builder().organisationFlag(false).build())
                .build())
            .build();
    }

    private void assertPdplDetails(String defendantAccountId, PdplIdentifierType debtorType) {
        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
        verify(loggingService).personalDataAccessLogAsync(captor.capture());

        PersonalDataProcessingLogDetails details = captor.getValue();
        assertEquals("View Defendant Account", details.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.CONSULTATION, details.getCategory());
        assertEquals(String.valueOf(USER_ID), details.getCreatedBy().getIdentifier());
        assertEquals(PdplIdentifierType.OPAL_USER_ID, details.getCreatedBy().getType());
        assertEquals(1, details.getIndividuals().size());
        assertEquals(defendantAccountId, details.getIndividuals().getFirst().getIdentifier());
        assertEquals(debtorType, details.getIndividuals().getFirst().getType());
        assertEquals(IP_ADDRESS, details.getIpAddress());
        assertEquals(NOW, details.getCreatedAt());
        assertNull(details.getRecipient());
    }
}
