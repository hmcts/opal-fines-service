package uk.gov.hmcts.opal.service.opal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
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
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.GetDefendantAccountConsolidatedAccountsResult;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.exception.PdplLoggingException;
import uk.gov.hmcts.opal.generated.model.ConsolidatedAccountDefendantAccount;
import uk.gov.hmcts.opal.generated.model.GetDefendantAccountHeaderSummary200Response;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountServiceProxy;

@ExtendWith(MockitoExtension.class)
class ConsolidatedAccountPdplLoggingServiceTest {

    private static final OffsetDateTime DATE_TIME = OffsetDateTime.parse("2026-10-19T10:15:30Z");
    private static final UserState USER_STATE = UserState.builder().userId(42L).build();

    private ConsolidatedAccountPdplLoggingService service;

    @Mock
    private LoggingService loggingService;

    @Mock
    private DefendantAccountServiceProxy defendantAccountServiceProxy;

    @BeforeEach
    void setUp() {
        service = new ConsolidatedAccountPdplLoggingService(loggingService,
            Clock.fixed(DATE_TIME.toInstant(), ZoneOffset.UTC), defendantAccountServiceProxy);
    }

    @Test
    void logIndividualChildAccounts_queuesOneLogWithDistinctIndividualDefendants() {
        when(defendantAccountServiceProxy.getHeaderSummary(101L)).thenReturn(headerWithOrganisationFlag(false));
        when(defendantAccountServiceProxy.getHeaderSummary(102L)).thenReturn(headerWithOrganisationFlag(false));
        when(defendantAccountServiceProxy.getHeaderSummary(103L)).thenReturn(headerWithOrganisationFlag(true));
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        try (MockedStatic<LogUtil> logUtil = mockStatic(LogUtil.class)) {
            logUtil.when(LogUtil::getIpAddress).thenReturn("192.0.2.1");
            service.logIndividualChildAccounts(USER_STATE, consolidatedResult(101L, 102L, 103L, 101L));
        }

        verify(defendantAccountServiceProxy, times(1)).getHeaderSummary(101L);
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
    void logIndividualChildAccounts_withOnlyOrganisationChildren_doesNotQueue() {
        when(defendantAccountServiceProxy.getHeaderSummary(103L)).thenReturn(headerWithOrganisationFlag(true));

        service.logIndividualChildAccounts(USER_STATE, consolidatedResult(103L));

        verifyNoInteractions(loggingService);
    }

    @Test
    void logIndividualChildAccounts_withNoChildren_doesNotQueueOrLookUpHeaders() {
        service.logIndividualChildAccounts(USER_STATE, consolidatedResult());
        service.logIndividualChildAccounts(USER_STATE, GetDefendantAccountConsolidatedAccountsResult.builder().build());

        verifyNoInteractions(loggingService, defendantAccountServiceProxy);
    }

    @Test
    void logIndividualChildAccounts_withMissingChildId_failsBeforeLogging() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> service.logIndividualChildAccounts(USER_STATE, consolidatedResult((Long) null)));

        assertThat(exception).hasMessage("Consolidated child account ID is missing");
        verifyNoInteractions(loggingService, defendantAccountServiceProxy);
    }

    @Test
    void logIndividualChildAccounts_withMissingHeader_failsBeforeLogging() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> service.logIndividualChildAccounts(USER_STATE, consolidatedResult(101L)));

        assertThat(exception).hasMessage("Organisation flag is missing for child account 101");
        verifyNoInteractions(loggingService);
    }

    @Test
    void logIndividualChildAccounts_withMissingHeaderResponse_failsBeforeLogging() {
        when(defendantAccountServiceProxy.getHeaderSummary(101L))
            .thenReturn(DefendantAccountHeaderSummary.builder().build());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> service.logIndividualChildAccounts(USER_STATE, consolidatedResult(101L)));

        assertThat(exception).hasMessage("Organisation flag is missing for child account 101");
        verifyNoInteractions(loggingService);
    }

    @Test
    void logIndividualChildAccounts_withMissingPartyDetails_failsBeforeLogging() {
        when(defendantAccountServiceProxy.getHeaderSummary(101L))
            .thenReturn(DefendantAccountHeaderSummary.builder()
                .response(GetDefendantAccountHeaderSummary200Response.builder().build())
                .build());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> service.logIndividualChildAccounts(USER_STATE, consolidatedResult(101L)));

        assertThat(exception).hasMessage("Organisation flag is missing for child account 101");
        verifyNoInteractions(loggingService);
    }

    @Test
    void logIndividualChildAccounts_withMissingOrganisationFlag_failsBeforeLogging() {
        when(defendantAccountServiceProxy.getHeaderSummary(101L)).thenReturn(headerWithOrganisationFlag(null));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> service.logIndividualChildAccounts(USER_STATE, consolidatedResult(101L)));

        assertThat(exception).hasMessage("Organisation flag is missing for child account 101");
        verifyNoInteractions(loggingService);
    }

    @Test
    void logIndividualChildAccounts_whenQueueRejects_throwsPdplLoggingException() {
        when(defendantAccountServiceProxy.getHeaderSummary(101L)).thenReturn(headerWithOrganisationFlag(false));
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(false);

        PdplLoggingException exception = assertThrows(PdplLoggingException.class,
            () -> service.logIndividualChildAccounts(USER_STATE, consolidatedResult(101L)));

        assertThat(exception).hasMessage("Unable to queue PDPO log for consolidated accounts");
        verify(loggingService).personalDataAccessLogAsync(any());
    }

    @Test
    void logIndividualChildAccounts_whenQueueThrows_wrapsPdplLoggingException() {
        when(defendantAccountServiceProxy.getHeaderSummary(101L)).thenReturn(headerWithOrganisationFlag(false));
        RuntimeException queueFailure = new RuntimeException("queue unavailable");
        when(loggingService.personalDataAccessLogAsync(any())).thenThrow(queueFailure);

        PdplLoggingException exception = assertThrows(PdplLoggingException.class,
            () -> service.logIndividualChildAccounts(USER_STATE, consolidatedResult(101L)));

        assertThat(exception).hasMessage("Unable to queue PDPO log for consolidated accounts")
            .hasCause(queueFailure);
        verify(loggingService).personalDataAccessLogAsync(any());
    }

    private GetDefendantAccountConsolidatedAccountsResult consolidatedResult(Long... childIds) {
        List<ConsolidatedAccountDefendantAccount> children = Arrays.stream(childIds)
            .map(id -> ConsolidatedAccountDefendantAccount.builder().accountId(id).build())
            .toList();
        return GetDefendantAccountConsolidatedAccountsResult.builder().payload(children).build();
    }

    private DefendantAccountHeaderSummary headerWithOrganisationFlag(Boolean organisationFlag) {
        return DefendantAccountHeaderSummary.builder()
            .response(GetDefendantAccountHeaderSummary200Response.builder()
                .partyDetails(PartyDetailsCommon.builder().organisationFlag(organisationFlag).build())
                .build())
            .build();
    }
}
