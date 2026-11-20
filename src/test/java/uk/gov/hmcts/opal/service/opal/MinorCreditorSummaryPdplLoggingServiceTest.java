package uk.gov.hmcts.opal.service.opal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.controllers.util.UserStateUtil;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.exception.PdplLoggingException;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

@ExtendWith(MockitoExtension.class)
class MinorCreditorSummaryPdplLoggingServiceTest {

    @Mock
    private LoggingService loggingService;

    private MinorCreditorSummaryPdplLoggingService minorCreditorSummaryPdplLoggingService;

    private UserState userState;

    @BeforeEach
    void setUp() {
        userState = UserStateUtil.allFinesPermissionUser();

        Clock clock = Clock.fixed(Instant.parse("2026-10-20T17:00:00Z"), ZoneOffset.UTC);
        minorCreditorSummaryPdplLoggingService = new MinorCreditorSummaryPdplLoggingService(loggingService, clock);
    }

    @Test
    void logIndividualMinorCreditorSummary_individualMinorCreditorQueueLog() {
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        minorCreditorSummaryPdplLoggingService.logIndividualMinorCreditorSummary(123L, userState,
            buildResponse(false));

        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);

        verify(loggingService).personalDataAccessLogAsync(captor.capture());

        PersonalDataProcessingLogDetails logDetails = captor.getValue();
        assertThat(logDetails).isNotNull();
        assertThat(logDetails.getBusinessIdentifier()).isEqualTo("View Minor Creditor Account - Individual");
        assertThat(logDetails.getCategory()).isEqualTo(PersonalDataProcessingCategory.CONSULTATION);
        assertThat(logDetails.getIndividuals().getFirst().getIdentifier()).isEqualTo("123");
        assertThat(logDetails.getIndividuals().getFirst().getType()).isEqualTo(PdplIdentifierType.MINOR_CREDITOR);
        assertThat(logDetails.getCreatedBy().getIdentifier()).isEqualTo("0");
        assertThat(logDetails.getCreatedBy().getType()).isEqualTo(PdplIdentifierType.OPAL_USER_ID);
        assertThat(logDetails.getCreatedAt()).isEqualTo(OffsetDateTime.parse("2026-10-20T17:00:00Z"));
        assertThat(logDetails.getRecipient()).isNull();
    }

    @Test
    void logIndividualMinorCreditorSummary_companyMinorCreditorDoesNotQueueLog() {
        minorCreditorSummaryPdplLoggingService.logIndividualMinorCreditorSummary(123L, userState, buildResponse(true));

        verify(loggingService, never()).personalDataAccessLogAsync(any());
    }

    @Test
    void logIndividualMinorCreditorSummary_rejectedQueueThrowsPdplLoggingException() {
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(false);

        assertThatThrownBy(
            () -> minorCreditorSummaryPdplLoggingService.logIndividualMinorCreditorSummary(123L, userState,
                buildResponse(false))).isInstanceOf(PdplLoggingException.class)
            .hasMessageContaining("Unable to queue PDPO log for minor creditor account");
    }

    @Test
    void logIndividualMinorCreditorSummary_queueErrorThrowsPdplLoggingException() {
        when(loggingService.personalDataAccessLogAsync(any())).thenThrow(new RuntimeException("queue unavailable"));

        assertThatThrownBy(
            () -> minorCreditorSummaryPdplLoggingService.logIndividualMinorCreditorSummary(123L, userState,
                buildResponse(false))).isInstanceOf(PdplLoggingException.class)
            .hasMessageContaining("Unable to queue PDPO log for minor creditor account");
    }

    private MinorCreditorAccountHeaderSummaryResponse buildResponse(boolean organisation) {
        return new MinorCreditorAccountHeaderSummaryResponse().party(
            new PartyDetailsCommon().organisationFlag(organisation));
    }
}
