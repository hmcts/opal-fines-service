package uk.gov.hmcts.opal.service.opal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import uk.gov.hmcts.opal.service.opal.DraftAccountPdplLoggingService.Action;
import uk.gov.hmcts.opal.util.LogUtil;

@ExtendWith(MockitoExtension.class)
class DraftAccountPdplLoggingServiceTest {

    @Mock
    private LoggingService loggingService;

    private DraftAccountPdplLoggingService serviceWithNow(OffsetDateTime now) {
        Clock fixedClock = Clock.fixed(now.toInstant(), ZoneOffset.UTC);
        return new DraftAccountPdplLoggingService(loggingService, fixedClock);
    }

    @Test
    @DisplayName("Submit - Defendant: full payload asserted")
    void submitDefendant_fullPayload() {
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
        OffsetDateTime expectedNow = OffsetDateTime.parse("2023-01-02T03:04:05Z");

        DraftAccountPdplLoggingService pdplLoggingService = serviceWithNow(expectedNow);

        try (MockedStatic<LogUtil> logUtilMock = Mockito.mockStatic(LogUtil.class)) {
            logUtilMock.when(LogUtil::getIpAddress).thenReturn(expectedIp);
            pdplLoggingService.pdplForDraftAccount(entity, Action.SUBMIT);
        }

        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
        verify(loggingService, times(1)).personalDataAccessLogAsync(captor.capture());

        PersonalDataProcessingLogDetails details = captor.getValue();

        assertEquals(expectedBusinessIdentifier, details.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.COLLECTION, details.getCategory());
        assertEquals(expectedIp, details.getIpAddress());
        assertEquals(expectedNow, details.getCreatedAt());
        assertThat(details.getRecipient()).isNull();

        ParticipantIdentifier by = details.getCreatedBy();
        assertThat(by).isNotNull();
        assertEquals(submittedBy, by.getIdentifier());
        assertEquals(PdplIdentifierType.OPAL_USER_ID, by.getType());

        List<ParticipantIdentifier> individuals = details.getIndividuals();
        assertThat(individuals).hasSize(1);
        ParticipantIdentifier ind = individuals.get(0);
        assertEquals(draftId.toString(), ind.getIdentifier());
        assertEquals(PdplIdentifierType.DRAFT_ACCOUNT, ind.getType());
    }

    @Test
    @DisplayName("Submit - Parent/Guardian and Defendant: asserted order and payloads")
    void submitPg_thenDefendant_fullPayloads() {
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

        DraftAccountPdplLoggingService pdplLoggingService = serviceWithNow(expectedNow);

        try (MockedStatic<LogUtil> logUtilMock = Mockito.mockStatic(LogUtil.class)) {
            logUtilMock.when(LogUtil::getIpAddress).thenReturn(expectedIp);
            pdplLoggingService.pdplForDraftAccount(entity, Action.SUBMIT);
        }

        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
        verify(loggingService, times(2)).personalDataAccessLogAsync(captor.capture());

        List<PersonalDataProcessingLogDetails> calls = captor.getAllValues();
        assertEquals(2, calls.size());

        PersonalDataProcessingLogDetails first = calls.get(0);
        assertEquals("Submit Draft Account - Parent or Guardian", first.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.COLLECTION, first.getCategory());
        assertEquals(expectedIp, first.getIpAddress());
        assertEquals(expectedNow, first.getCreatedAt());
        assertThat(first.getRecipient()).isNull();
        assertEquals("33", first.getIndividuals().get(0).getIdentifier());
        assertEquals("user-pg", first.getCreatedBy().getIdentifier());

        PersonalDataProcessingLogDetails second = calls.get(1);
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

        DraftAccountPdplLoggingService pdplLoggingService = serviceWithNow(expectedNow);

        try (MockedStatic<LogUtil> logUtilMock = Mockito.mockStatic(LogUtil.class)) {
            logUtilMock.when(LogUtil::getIpAddress).thenReturn(expectedIp);
            pdplLoggingService.pdplForDraftAccount(entity, Action.SUBMIT);
        }

        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
        verify(loggingService, times(2)).personalDataAccessLogAsync(captor.capture());

        List<PersonalDataProcessingLogDetails> calls = captor.getAllValues();

        PersonalDataProcessingLogDetails defendantDetails = calls.get(0);
        assertEquals("Submit Draft Account - Defendant", defendantDetails.getBusinessIdentifier());
        assertEquals(expectedIp, defendantDetails.getIpAddress());
        assertEquals(expectedNow, defendantDetails.getCreatedAt());
        assertEquals("user-min", defendantDetails.getCreatedBy().getIdentifier());
        assertEquals("22", defendantDetails.getIndividuals().get(0).getIdentifier());

        PersonalDataProcessingLogDetails minorDetails = calls.get(1);
        assertEquals("Submit Draft Account - Minor Creditor", minorDetails.getBusinessIdentifier());
        assertEquals(expectedIp, minorDetails.getIpAddress());
        assertEquals(expectedNow, minorDetails.getCreatedAt());
        assertEquals("user-min", minorDetails.getCreatedBy().getIdentifier());
        assertEquals("22", minorDetails.getIndividuals().get(0).getIdentifier());
    }

    // ---- RESUBMIT tests added below ----

    @Test
    @DisplayName("Re-submit - Defendant: full payload asserted")
    void resubmitDefendant_fullPayload() {
        Long draftId = 12121212L;
        String submittedBy = "opal-user-resubmit";
        DraftAccountEntity entity = DraftAccountEntity.builder()
            .draftAccountId(draftId)
            .submittedBy(submittedBy)
            .account("""
                {
                  "account_type":"Fines",
                  "defendant_type":"adultOrYouthOnly",
                  "originator_name":"LJS",
                  "originator_id": 2,
                  "offences": []
                }
                """)
            .build();

        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        String expectedBusinessIdentifier = "Re-submit Draft Account - Defendant";
        String expectedIp = "198.51.100.7";
        OffsetDateTime expectedNow = OffsetDateTime.parse("2024-06-06T06:06:06Z");

        DraftAccountPdplLoggingService pdplLoggingService = serviceWithNow(expectedNow);

        try (MockedStatic<LogUtil> logUtilMock = Mockito.mockStatic(LogUtil.class)) {
            logUtilMock.when(LogUtil::getIpAddress).thenReturn(expectedIp);
            pdplLoggingService.pdplForDraftAccount(entity, Action.RESUBMIT);
        }

        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
        verify(loggingService, times(1)).personalDataAccessLogAsync(captor.capture());

        PersonalDataProcessingLogDetails details = captor.getValue();

        assertEquals(expectedBusinessIdentifier, details.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.COLLECTION, details.getCategory());
        assertEquals(expectedIp, details.getIpAddress());
        assertEquals(expectedNow, details.getCreatedAt());
        assertThat(details.getRecipient()).isNull();

        ParticipantIdentifier by = details.getCreatedBy();
        assertThat(by).isNotNull();
        assertEquals(submittedBy, by.getIdentifier());
        assertEquals(PdplIdentifierType.OPAL_USER_ID, by.getType());

        List<ParticipantIdentifier> individuals = details.getIndividuals();
        assertThat(individuals).hasSize(1);
        ParticipantIdentifier ind = individuals.get(0);
        assertEquals(draftId.toString(), ind.getIdentifier());
        assertEquals(PdplIdentifierType.DRAFT_ACCOUNT, ind.getType());
    }

    @Test
    @DisplayName("Re-submit - Parent/Guardian and Defendant: asserted order and payloads")
    void resubmitPg_thenDefendant_fullPayloads() {
        DraftAccountEntity entity = DraftAccountEntity.builder()
            .draftAccountId(44L)
            .submittedBy("user-resubmit-pg")
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

        String expectedIp = "203.0.113.9";
        OffsetDateTime expectedNow = OffsetDateTime.parse("2024-12-12T12:12:12Z");

        DraftAccountPdplLoggingService pdplLoggingService = serviceWithNow(expectedNow);

        try (MockedStatic<LogUtil> logUtilMock = Mockito.mockStatic(LogUtil.class)) {
            logUtilMock.when(LogUtil::getIpAddress).thenReturn(expectedIp);
            pdplLoggingService.pdplForDraftAccount(entity, Action.RESUBMIT);
        }

        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
        verify(loggingService, times(2)).personalDataAccessLogAsync(captor.capture());

        List<PersonalDataProcessingLogDetails> calls = captor.getAllValues();
        assertEquals(2, calls.size());

        PersonalDataProcessingLogDetails first = calls.get(0);
        assertEquals("Re-submit Draft Account - Parent or Guardian", first.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.COLLECTION, first.getCategory());
        assertEquals(expectedIp, first.getIpAddress());
        assertEquals(expectedNow, first.getCreatedAt());
        assertThat(first.getRecipient()).isNull();
        assertEquals("44", first.getIndividuals().get(0).getIdentifier());
        assertEquals("user-resubmit-pg", first.getCreatedBy().getIdentifier());

        PersonalDataProcessingLogDetails second = calls.get(1);
        assertEquals("Re-submit Draft Account - Defendant", second.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.COLLECTION, second.getCategory());
        assertEquals(expectedIp, second.getIpAddress());
        assertEquals(expectedNow, second.getCreatedAt());
        assertThat(second.getRecipient()).isNull();
        assertEquals("44", second.getIndividuals().get(0).getIdentifier());
        assertEquals("user-resubmit-pg", second.getCreatedBy().getIdentifier());
    }

    @Test
    @DisplayName("Re-submit - adultOrYouthOnly WITH minor -> Defendant + Minor Creditor PDPL in order")
    void resubmitDefendantAndMinor_fullPayloads() {
        DraftAccountEntity entity = DraftAccountEntity.builder()
            .draftAccountId(77L)
            .submittedBy("user-res-min")
            .account("""
            {
              "account_type":"Fines",
              "defendant_type":"adultOrYouthOnly",
              "originator_name":"LJS",
              "originator_id":7,
              "offences": [
                {
                  "impositions": [
                    {
                      "minor_creditor": {
                        "company_flag": false,
                        "surname": "Kid",
                        "forenames": "Bobby"
                      }
                    }
                  ]
                }
              ]
            }
            """)
            .build();

        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        String expectedIp = "198.51.100.21";
        OffsetDateTime expectedNow = OffsetDateTime.parse("2025-05-05T05:05:05Z");

        DraftAccountPdplLoggingService pdplLoggingService = serviceWithNow(expectedNow);

        try (MockedStatic<LogUtil> logUtilMock = Mockito.mockStatic(LogUtil.class)) {
            logUtilMock.when(LogUtil::getIpAddress).thenReturn(expectedIp);
            pdplLoggingService.pdplForDraftAccount(entity, Action.RESUBMIT);
        }

        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
        verify(loggingService, times(2)).personalDataAccessLogAsync(captor.capture());

        List<PersonalDataProcessingLogDetails> calls = captor.getAllValues();
        assertEquals(2, calls.size());

        PersonalDataProcessingLogDetails first = calls.get(0);
        assertEquals("Re-submit Draft Account - Defendant", first.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.COLLECTION, first.getCategory());
        assertEquals(expectedIp, first.getIpAddress());
        assertEquals(expectedNow, first.getCreatedAt());
        assertEquals("user-res-min", first.getCreatedBy().getIdentifier());
        assertEquals("77", first.getIndividuals().get(0).getIdentifier());

        PersonalDataProcessingLogDetails second = calls.get(1);
        assertEquals("Re-submit Draft Account - Minor Creditor", second.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.COLLECTION, second.getCategory());
        assertEquals(expectedIp, second.getIpAddress());
        assertEquals(expectedNow, second.getCreatedAt());
        assertEquals("user-res-min", second.getCreatedBy().getIdentifier());
        assertEquals("77", second.getIndividuals().get(0).getIdentifier());
    }

}
