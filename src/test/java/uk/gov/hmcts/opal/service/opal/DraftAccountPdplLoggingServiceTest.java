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

    private DraftAccountEntity makeEntity(Long draftId, String submittedBy, String accountJson) {
        return DraftAccountEntity.builder()
            .draftAccountId(draftId)
            .submittedBy(submittedBy)
            .account(accountJson)
            .build();
    }

    private List<PersonalDataProcessingLogDetails> runAndCapturePdpl(
        DraftAccountEntity entity,
        Action action,
        String expectedIp,
        OffsetDateTime expectedNow,
        int expectedCalls
    ) {
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        DraftAccountPdplLoggingService svc = serviceWithNow(expectedNow);

        try (MockedStatic<LogUtil> logUtilMock = Mockito.mockStatic(LogUtil.class)) {
            logUtilMock.when(LogUtil::getIpAddress).thenReturn(expectedIp);
            svc.pdplForDraftAccount(entity, action);
        }

        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);

        verify(loggingService, times(expectedCalls)).personalDataAccessLogAsync(captor.capture());

        return captor.getAllValues();
    }

    private void assertPdplCommon(
        PersonalDataProcessingLogDetails pdpl,
        String expectedBusinessIdentifier,
        PersonalDataProcessingCategory expectedCategory,
        String expectedIp,
        OffsetDateTime expectedNow,
        String expectedCreatedByIdentifier,
        String expectedIndividualIdentifier
    ) {
        assertEquals(expectedBusinessIdentifier, pdpl.getBusinessIdentifier());
        assertEquals(expectedCategory, pdpl.getCategory());
        assertEquals(expectedIp, pdpl.getIpAddress());
        assertEquals(expectedNow, pdpl.getCreatedAt());
        assertThat(pdpl.getRecipient()).isNull();

        assertThat(pdpl.getCreatedBy()).isNotNull();
        if (expectedCreatedByIdentifier != null) {
            assertEquals(expectedCreatedByIdentifier, pdpl.getCreatedBy().getIdentifier());
        }
        assertEquals(PdplIdentifierType.OPAL_USER_ID, pdpl.getCreatedBy().getType());

        List<ParticipantIdentifier> individuals = pdpl.getIndividuals();
        assertThat(individuals).hasSize(1);
        ParticipantIdentifier ind = individuals.get(0);
        assertEquals(expectedIndividualIdentifier, ind.getIdentifier());
        assertEquals(PdplIdentifierType.DRAFT_ACCOUNT, ind.getType());
    }

    @Test
    @DisplayName("Submit - Defendant: full payload asserted")
    void submitDefendant_fullPayload() {
        Long draftId = 11111111L;
        String submittedBy = "opal-user-99";
        DraftAccountEntity entity = makeEntity(draftId, submittedBy, """
            {
              "account_type":"Fines",
              "defendant_type":"adultOrYouthOnly",
              "originator_name":"LJS",
              "originator_id": 1,
              "offences": []
            }
            """);

        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        String expectedBusinessIdentifier = "Submit Draft Account - Defendant";
        String expectedIp = "192.0.2.33";
        OffsetDateTime expectedNow = OffsetDateTime.parse("2023-01-02T03:04:05Z");

        List<PersonalDataProcessingLogDetails> calls =
            runAndCapturePdpl(entity, Action.SUBMIT, expectedIp, expectedNow, 1);

        PersonalDataProcessingLogDetails details = calls.get(0);

        assertPdplCommon(details,
            "Submit Draft Account - Defendant",
            PersonalDataProcessingCategory.COLLECTION,
            expectedIp,
            expectedNow,
            submittedBy,
            draftId.toString());
    }

    @Test
    @DisplayName("Submit - Parent/Guardian and Defendant: asserted order and payloads")
    void submitPg_thenDefendant_fullPayloads() {
        DraftAccountEntity entity = makeEntity(33L, "user-pg", """
            {
              "account_type":"Fines",
              "defendant_type":"pgToPay",
              "originator_name":"LJS",
              "originator_id":4,
              "offences": []
            }
            """);

        String expectedIp = "203.0.113.5";
        OffsetDateTime expectedNow = OffsetDateTime.parse("2025-04-04T04:04:04Z");

        List<PersonalDataProcessingLogDetails> calls =
            runAndCapturePdpl(entity, Action.SUBMIT, expectedIp, expectedNow, 2);

        PersonalDataProcessingLogDetails first = calls.get(0);
        assertPdplCommon(first,
            "Submit Draft Account - Parent or Guardian",
            PersonalDataProcessingCategory.COLLECTION,
            expectedIp,
            expectedNow,
            "user-pg",
            "33");

        PersonalDataProcessingLogDetails second = calls.get(1);
        assertPdplCommon(second,
            "Submit Draft Account - Defendant",
            PersonalDataProcessingCategory.COLLECTION,
            expectedIp,
            expectedNow,
            "user-pg",
            "33");
    }

    @Test
    @DisplayName("Submit - Minor Creditor present -> logs Defendant then Minor Creditor; both payloads asserted")
    void submitDefendantAndMinor_fullPayloads() {
        DraftAccountEntity entity = makeEntity(22L, "user-min", """
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
            """);

        String expectedIp = "192.0.2.1";
        OffsetDateTime expectedNow = OffsetDateTime.parse("2025-03-03T03:03:03Z");

        List<PersonalDataProcessingLogDetails> calls =
            runAndCapturePdpl(entity, Action.SUBMIT, expectedIp, expectedNow, 2);

        PersonalDataProcessingLogDetails defendantDetails = calls.get(0);
        assertPdplCommon(defendantDetails,
            "Submit Draft Account - Defendant",
            PersonalDataProcessingCategory.COLLECTION,
            expectedIp,
            expectedNow,
            "user-min",
            "22");

        PersonalDataProcessingLogDetails minorDetails = calls.get(1);
        assertPdplCommon(minorDetails,
            "Submit Draft Account - Minor Creditor",
            PersonalDataProcessingCategory.COLLECTION,
            expectedIp,
            expectedNow,
            "user-min",
            "22");
    }

    @Test
    @DisplayName("Re-submit - Defendant: full payload asserted")
    void resubmitDefendant_fullPayload() {
        Long draftId = 12121212L;
        String submittedBy = "opal-user-resubmit";
        DraftAccountEntity entity = makeEntity(draftId, submittedBy, """
            {
              "account_type":"Fines",
              "defendant_type":"adultOrYouthOnly",
              "originator_name":"LJS",
              "originator_id": 2,
              "offences": []
            }
            """);

        String expectedIp = "198.51.100.7";
        OffsetDateTime expectedNow = OffsetDateTime.parse("2024-06-06T06:06:06Z");

        List<PersonalDataProcessingLogDetails> calls =
            runAndCapturePdpl(entity, Action.RESUBMIT, expectedIp, expectedNow, 1);

        PersonalDataProcessingLogDetails details = calls.get(0);

        assertPdplCommon(details,
            "Re-submit Draft Account - Defendant",
            PersonalDataProcessingCategory.COLLECTION,
            expectedIp,
            expectedNow,
            submittedBy,
            draftId.toString());
    }

    @Test
    @DisplayName("Re-submit - Parent/Guardian and Defendant: asserted order and payloads")
    void resubmitPg_thenDefendant_fullPayloads() {
        DraftAccountEntity entity = makeEntity(44L, "user-resubmit-pg", """
            {
              "account_type":"Fines",
              "defendant_type":"pgToPay",
              "originator_name":"LJS",
              "originator_id":5,
              "offences": []
            }
            """);

        String expectedIp = "203.0.113.9";
        OffsetDateTime expectedNow = OffsetDateTime.parse("2024-12-12T12:12:12Z");

        List<PersonalDataProcessingLogDetails> calls =
            runAndCapturePdpl(entity, Action.RESUBMIT, expectedIp, expectedNow, 2);

        PersonalDataProcessingLogDetails first = calls.get(0);
        assertPdplCommon(first,
            "Re-submit Draft Account - Parent or Guardian",
            PersonalDataProcessingCategory.COLLECTION,
            expectedIp,
            expectedNow,
            "user-resubmit-pg",
            "44");

        PersonalDataProcessingLogDetails second = calls.get(1);
        assertPdplCommon(second,
            "Re-submit Draft Account - Defendant",
            PersonalDataProcessingCategory.COLLECTION,
            expectedIp,
            expectedNow,
            "user-resubmit-pg",
            "44");
    }

    @Test
    @DisplayName("Update - Defendant: full payload asserted")
    void replaceDefendant_fullPayload() {
        Long draftId = 55555555L;
        String submittedBy = "opal-user-replace";
        DraftAccountEntity entity = makeEntity(draftId, submittedBy, """
            {
              "account_type":"Fines",
              "defendant_type":"adultOrYouthOnly",
              "originator_name":"LJS",
              "originator_id": 7,
              "offences": []
            }
            """);

        String expectedIp = "198.51.100.9";
        OffsetDateTime expectedNow = OffsetDateTime.parse("2025-01-01T01:01:01Z");

        List<PersonalDataProcessingLogDetails> calls =
            runAndCapturePdpl(entity, Action.REPLACE, expectedIp, expectedNow, 1);

        PersonalDataProcessingLogDetails details = calls.get(0);

        assertPdplCommon(details,
            "Update Draft Account - Defendant",
            PersonalDataProcessingCategory.COLLECTION,
            expectedIp,
            expectedNow,
            submittedBy,
            draftId.toString());
    }

    @Test
    @DisplayName("Update - Parent/Guardian and Defendant: asserted order and payloads")
    void replacePg_thenDefendant_fullPayloads() {
        DraftAccountEntity entity = makeEntity(66L, "user-replace-pg", """
            {
              "account_type":"Fines",
              "defendant_type":"pgToPay",
              "originator_name":"LJS",
              "originator_id":8,
              "offences": []
            }
            """);

        String expectedIp = "203.0.113.11";
        OffsetDateTime expectedNow = OffsetDateTime.parse("2025-02-02T02:02:02Z");

        List<PersonalDataProcessingLogDetails> calls =
            runAndCapturePdpl(entity, Action.REPLACE, expectedIp, expectedNow, 2);

        PersonalDataProcessingLogDetails first = calls.get(0);
        assertPdplCommon(first,
            "Update Draft Account - Parent or Guardian",
            PersonalDataProcessingCategory.COLLECTION,
            expectedIp,
            expectedNow,
            "user-replace-pg",
            "66");

        PersonalDataProcessingLogDetails second = calls.get(1);
        assertPdplCommon(second,
            "Update Draft Account - Defendant",
            PersonalDataProcessingCategory.COLLECTION,
            expectedIp,
            expectedNow,
            "user-replace-pg",
            "66");
    }

}
