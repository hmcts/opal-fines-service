package uk.gov.hmcts.opal.service.opal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;
import uk.gov.hmcts.opal.util.LogUtil;

@ExtendWith(MockitoExtension.class)
public class PdplLoggingServiceTest {

    @Mock
    private LoggingService loggingService;

    @Mock
    private Clock clock;

    @InjectMocks
    private PdplLoggingService pdplLoggingService;

    @Test
    @DisplayName("Submit - Defendant: full payload asserted")
    void submitDefendant_fullPayload() {
        // Arrange
        Long draftId = 11111111L;
        String submittedBy = "opal-user-99";
        DraftAccountEntity entity = DraftAccountEntity.builder()
            .draftAccountId(draftId)
            .submittedBy(submittedBy)
            .account("""
                {
                  "account_type":"Fines",
                  "defendant_type":"adultOrYouthOnly",
                  "originator_name":"LJS",
                  "originator_id": 1,
                  "offences": []
                }
                """)
            .build();

        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        String expectedBusinessIdentifier = "Submit Draft Account - Defendant";
        String expectedIp = "192.0.2.33";
        OffsetDateTime expectedNow = OffsetDateTime.parse("2023-01-02T03:04:05+00:00");

        try (MockedStatic<LogUtil> logUtilMock = Mockito.mockStatic(LogUtil.class)) {
            logUtilMock.when(LogUtil::getIpAddress).thenReturn(expectedIp);
            logUtilMock.when(() -> LogUtil.getCurrentDateTime(clock)).thenReturn(expectedNow);

            // Act
            pdplLoggingService.pdplForSubmitDraftAccount(entity);
        }

        // Assert one call and full payload
        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
        verify(loggingService, times(1)).personalDataAccessLogAsync(captor.capture());

        PersonalDataProcessingLogDetails details = captor.getValue();

        assertEquals(expectedBusinessIdentifier, details.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.COLLECTION, details.getCategory());
        assertEquals(expectedIp, details.getIpAddress());
        assertEquals(expectedNow, details.getCreatedAt());
        assertThat(details.getRecipient()).isNull();

        // createdBy checks
        ParticipantIdentifier by = details.getCreatedBy();
        assertThat(by).isNotNull();
        assertEquals(submittedBy, by.getIdentifier());
        assertEquals(PdplIdentifierType.OPAL_USER_ID, by.getType());

        // individuals checks
        List<ParticipantIdentifier> individuals = details.getIndividuals();
        assertThat(individuals).hasSize(1);
        ParticipantIdentifier ind = individuals.get(0);
        assertEquals(draftId.toString(), ind.getIdentifier());
        assertEquals(PdplIdentifierType.DRAFT_ACCOUNT, ind.getType());
    }

    @Test
    @DisplayName("Submit - Parent/Guardian and Defendant: asserted order and payloads")
    void submitPg_thenDefendant_fullPayloads() {
        // Arrange
        DraftAccountEntity entity = DraftAccountEntity.builder()
            .draftAccountId(33L)
            .submittedBy("user-pg")
            .account("""
                {
                  "account_type":"Fines",
                  "defendant_type":"pgToPay",
                  "originator_name":"LJS",
                  "originator_id":4,
                  "offences": []
                }
                """)
            .build();

        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        String expectedIp = "203.0.113.5";
        OffsetDateTime expectedNow = OffsetDateTime.parse("2025-04-04T04:04:04Z");

        try (MockedStatic<LogUtil> logUtilMock = Mockito.mockStatic(LogUtil.class)) {
            logUtilMock.when(LogUtil::getIpAddress).thenReturn(expectedIp);
            logUtilMock.when(() -> LogUtil.getCurrentDateTime(clock)).thenReturn(expectedNow);

            // Act
            pdplLoggingService.pdplForSubmitDraftAccount(entity);
        }

        // Assert two calls in that exact order
        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
        verify(loggingService, times(2)).personalDataAccessLogAsync(captor.capture());

        List<PersonalDataProcessingLogDetails> calls = captor.getAllValues();
        assertEquals(2, calls.size());

        PersonalDataProcessingLogDetails first = calls.get(0);

        // first: Parent or Guardian
        assertEquals("Submit Draft Account - Parent or Guardian", first.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.COLLECTION, first.getCategory());
        assertEquals(expectedIp, first.getIpAddress());
        assertEquals(expectedNow, first.getCreatedAt());
        assertThat(first.getRecipient()).isNull();
        assertEquals("33", first.getIndividuals().get(0).getIdentifier());
        assertEquals("user-pg", first.getCreatedBy().getIdentifier());

        PersonalDataProcessingLogDetails second = calls.get(1);

        // second: Defendant
        assertEquals("Submit Draft Account - Defendant", second.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.COLLECTION, second.getCategory());
        assertEquals(expectedIp, second.getIpAddress());
        assertEquals(expectedNow, second.getCreatedAt());
        assertThat(second.getRecipient()).isNull();
        assertEquals("33", second.getIndividuals().get(0).getIdentifier());
        assertEquals("user-pg", second.getCreatedBy().getIdentifier());
    }

    @Test
    @DisplayName("Submit - Minor Creditor present -> logs Defendant then Minor Creditor; both payloads asserted")
    void submitDefendantAndMinor_fullPayloads() {
        // Arrange
        DraftAccountEntity entity = DraftAccountEntity.builder()
            .draftAccountId(22L)
            .submittedBy("user-min")
            .account("""
                {
                  "account_type":"Fines",
                  "defendant_type":"adultOrYouthOnly",
                  "originator_name":"LJS",
                  "originator_id":3,
                  "offences": [
                    {
                      "impositions": [
                        {
                          "minor_creditor": {
                            "company_flag": false,
                            "surname": "Minor",
                            "forenames": "Alice"
                          }
                        }
                      ]
                    }
                  ]
                }
                """)
            .build();

        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        String expectedIp = "192.0.2.1";
        OffsetDateTime expectedNow = OffsetDateTime.parse("2025-03-03T03:03:03Z");

        try (MockedStatic<LogUtil> logUtilMock = Mockito.mockStatic(LogUtil.class)) {
            logUtilMock.when(LogUtil::getIpAddress).thenReturn(expectedIp);
            logUtilMock.when(() -> LogUtil.getCurrentDateTime(clock)).thenReturn(expectedNow);

            // Act
            pdplLoggingService.pdplForSubmitDraftAccount(entity);
        }

        // Assert two calls
        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
        verify(loggingService, times(2)).personalDataAccessLogAsync(captor.capture());

        List<PersonalDataProcessingLogDetails> calls = captor.getAllValues();

        // Validate first (Defendant)
        PersonalDataProcessingLogDetails defendantDetails = calls.get(0);
        assertEquals("Submit Draft Account - Defendant", defendantDetails.getBusinessIdentifier());
        assertEquals(expectedIp, defendantDetails.getIpAddress());
        assertEquals(expectedNow, defendantDetails.getCreatedAt());
        assertEquals("user-min", defendantDetails.getCreatedBy().getIdentifier());
        assertEquals("22", defendantDetails.getIndividuals().get(0).getIdentifier());

        // Validate second (Minor Creditor)
        PersonalDataProcessingLogDetails minorDetails = calls.get(1);
        assertEquals("Submit Draft Account - Minor Creditor", minorDetails.getBusinessIdentifier());
        assertEquals(expectedIp, minorDetails.getIpAddress());
        assertEquals(expectedNow, minorDetails.getCreatedAt());
        assertEquals("user-min", minorDetails.getCreatedBy().getIdentifier());
        assertEquals("22", minorDetails.getIndividuals().get(0).getIdentifier());
    }

    @Test
    @DisplayName("Re-submit (update) - pgToPay -> Re-submit Parent/Guardian "
        + "and Re-submit Defendant with expected payloads")
    void resubmit_pgToPay_payloads() {
        // Arrange
        DraftAccountEntity entity = DraftAccountEntity.builder()
            .draftAccountId(44L)
            .submittedBy("user-resubmit")
            .account("""
                {
                  "account_type":"Fines",
                  "defendant_type":"pgToPay",
                  "originator_name":"LJS",
                  "originator_id":5,
                  "offences": []
                }
                """)
            .build();

        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        String expectedIp = "198.51.100.7";
        OffsetDateTime expectedNow = OffsetDateTime.parse("2025-05-05T05:05:05Z");

        try (MockedStatic<LogUtil> logUtil = Mockito.mockStatic(LogUtil.class)) {
            logUtil.when(LogUtil::getIpAddress).thenReturn(expectedIp);
            logUtil.when(() -> LogUtil.getCurrentDateTime(clock)).thenReturn(expectedNow);

            // Act
            pdplLoggingService.pdplForUpdateDraftAccount(entity);
        }

        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
        verify(loggingService, times(2)).personalDataAccessLogAsync(captor.capture());

        List<PersonalDataProcessingLogDetails> calls = captor.getAllValues();
        List<String> identifiers = calls.stream()
            .map(PersonalDataProcessingLogDetails::getBusinessIdentifier)
            .collect(Collectors.toList());

        assertEquals(List.of(
            "Re-submit Draft Account - Parent or Guardian",
            "Re-submit Draft Account - Defendant"
        ), identifiers);

        // Assert createdAt and ip were set on one of the calls
        assertEquals(expectedIp, calls.get(0).getIpAddress());
        assertEquals(expectedNow, calls.get(0).getCreatedAt());
    }

    @Test
    @DisplayName("company defendant_type -> no PDPL logging (asserted no interactions)")
    void company_noLogging_assertNoCalls() {
        DraftAccountEntity entity = DraftAccountEntity.builder()
            .draftAccountId(1L)
            .submittedBy("user-x")
            .account("""
                {
                  "account_type":"Fines",
                  "defendant_type":"company",
                  "originator_name":"LJS",
                  "originator_id": 1
                }
                """)
            .build();

        pdplLoggingService.pdplForSubmitDraftAccount(entity);

        verify(loggingService, times(0)).personalDataAccessLogAsync(any());
    }

    @Test
    @DisplayName("minor_creditor path returns null -> no minor logging")
    void minorCreditorsNull_noLogging() {
        // Build account with no minor_creditor entries (only defendant)
        DraftAccountEntity entity = DraftAccountEntity.builder()
            .draftAccountId(77L)
            .submittedBy("no-minor")
            .account("""
                {
                  "account_type":"Fines",
                  "defendant_type":"adultOrYouthOnly",
                  "originator_name":"LJS",
                  "originator_id":7,
                  "offences": [
                    {
                      "impositions": [
                        { "some_other_field": 1 }
                      ]
                    }
                  ]
                }
                """)
            .build();

        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        String expectedIp = "10.0.0.8";
        OffsetDateTime expectedNow = OffsetDateTime.parse("2026-01-01T00:00:00Z");

        try (MockedStatic<LogUtil> logUtil = Mockito.mockStatic(LogUtil.class)) {
            logUtil.when(LogUtil::getIpAddress).thenReturn(expectedIp);
            logUtil.when(() -> LogUtil.getCurrentDateTime(clock)).thenReturn(expectedNow);

            pdplLoggingService.pdplForSubmitDraftAccount(entity);
        }
        // Only defendant log should have been called once
        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
        verify(loggingService, times(1)).personalDataAccessLogAsync(captor.capture());

        PersonalDataProcessingLogDetails details = captor.getValue();
        assertEquals("Submit Draft Account - Defendant", details.getBusinessIdentifier());
        assertEquals(expectedIp, details.getIpAddress());
        assertEquals(expectedNow, details.getCreatedAt());
    }
}
