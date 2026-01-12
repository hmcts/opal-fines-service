package uk.gov.hmcts.opal.service.opal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
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
import uk.gov.hmcts.opal.util.JsonPathUtil;
import uk.gov.hmcts.opal.util.LogUtil;

@ExtendWith(MockitoExtension.class)
public class PdplLoggingServiceTest {

    @Mock
    LoggingService loggingService;

    @Mock
    Clock clock; // injected into PdplLoggingService, used to match static LogUtil call

    @InjectMocks
    PdplLoggingService pdplLoggingService;

    @Test
    void logDefendantInfo_passes_expected_logDetails_to_loggingService() {
        // Arrange
        Long draftId = 11111111L;
        String submittedBy = "opal-user-99";
        DraftAccountEntity entity = Mockito.mock(DraftAccountEntity.class);
        Mockito.when(entity.getDraftAccountId()).thenReturn(draftId);
        Mockito.when(entity.getSubmittedBy()).thenReturn(submittedBy);

        // stub loggingService boolean-return (method returns boolean)
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        String expectedBusinessIdentifier = "Submit Draft Account - Defendant";
        String expectedIp = "192.0.2.33";
        OffsetDateTime expectedNow = OffsetDateTime.parse("2023-01-02T03:04:05+00:00");

        try (MockedStatic<LogUtil> logUtilMock = Mockito.mockStatic(LogUtil.class)) {
            logUtilMock.when(LogUtil::getIpAddress).thenReturn(expectedIp);
            logUtilMock.when(() -> LogUtil.getCurrentDateTime(clock)).thenReturn(expectedNow);

            // Act - direct call (package-private method, same package)
            pdplLoggingService.logSubmitDraftAccountDefendantInfo(entity);

            // Assert
            ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
                ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);

            verify(loggingService, times(1)).personalDataAccessLogAsync(captor.capture());

            PersonalDataProcessingLogDetails captured = captor.getValue();

            // Validate full details exactly as built in your method
            assertThat(captured.getBusinessIdentifier()).isEqualTo(expectedBusinessIdentifier);
            assertThat(captured.getCategory()).isEqualTo(PersonalDataProcessingCategory.COLLECTION);
            assertThat(captured.getIpAddress()).isEqualTo(expectedIp);
            assertThat(captured.getCreatedAt()).isEqualTo(expectedNow);

            ParticipantIdentifier createdBy = captured.getCreatedBy();
            assertThat(createdBy).isNotNull();
            assertThat(createdBy.getIdentifier()).isEqualTo(submittedBy);
            assertThat(createdBy.getType()).isEqualTo(PdplIdentifierType.OPAL_USER_ID);

            List<ParticipantIdentifier> individuals = captured.getIndividuals();
            assertThat(individuals).hasSize(1);
            ParticipantIdentifier individual = individuals.get(0);
            assertThat(individual.getIdentifier()).isEqualTo(draftId.toString());
            assertThat(individual.getType()).isEqualTo(PdplIdentifierType.DRAFT_ACCOUNT);

            assertThat(captured.getRecipient()).isNull();
        }
    }

    @Test
    void logParentGuardianInfo_passes_expected_logDetails_to_loggingService() {
        // Arrange
        Long draftId = 12345678L;
        String submittedBy = "user-42";
        DraftAccountEntity entity = Mockito.mock(DraftAccountEntity.class);
        Mockito.when(entity.getDraftAccountId()).thenReturn(draftId);
        Mockito.when(entity.getSubmittedBy()).thenReturn(submittedBy);

        // stub loggingService boolean-return
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        String expectedBusinessIdentifier = "Submit Draft Account - Parent or Guardian";
        String expectedIp = "10.0.0.5";
        OffsetDateTime expectedNow = OffsetDateTime.parse("2024-02-03T04:05:06+00:00");

        try (MockedStatic<LogUtil> logUtilMock = Mockito.mockStatic(LogUtil.class)) {
            logUtilMock.when(LogUtil::getIpAddress).thenReturn(expectedIp);
            logUtilMock.when(() -> LogUtil.getCurrentDateTime(clock)).thenReturn(expectedNow);

            // Act - direct call
            pdplLoggingService.logSubmitDraftAccountParentGuardianInfo(entity);

            // Assert
            ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
                ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);

            verify(loggingService, times(1)).personalDataAccessLogAsync(captor.capture());

            PersonalDataProcessingLogDetails captured = captor.getValue();

            assertThat(captured.getBusinessIdentifier()).isEqualTo(expectedBusinessIdentifier);
            assertThat(captured.getIpAddress()).isEqualTo(expectedIp);
            assertThat(captured.getCreatedAt()).isEqualTo(expectedNow);

            // spot-check createdBy and individuals are correctly populated
            assertThat(captured.getCreatedBy().getIdentifier()).isEqualTo(submittedBy);
            assertThat(captured.getIndividuals()).hasSize(1);
            assertThat(captured.getIndividuals().get(0).getIdentifier()).isEqualTo(draftId.toString());
        }
    }

    @Test
    void logMinorCreditorInfo_passes_expected_logDetails_to_loggingService() {
        // Arrange
        Long draftId = 99999999L;
        String submittedBy = "user-minor";
        DraftAccountEntity entity = Mockito.mock(DraftAccountEntity.class);
        Mockito.when(entity.getDraftAccountId()).thenReturn(draftId);
        Mockito.when(entity.getSubmittedBy()).thenReturn(submittedBy);

        // stub loggingService boolean-return
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        String expectedBusinessIdentifier = "Submit Draft Account - Minor Creditor";
        String expectedIp = "203.0.113.7";
        OffsetDateTime expectedNow = OffsetDateTime.parse("2025-03-04T05:06:07+00:00");

        // create a mocked DocContext and stub the read(...) to return a minor_creditor list
        JsonPathUtil.DocContext docContext = Mockito.mock(JsonPathUtil.DocContext.class);

        // The code expects a List<Map<String,Object>> where entries include "company_flag"
        List<Map<String, Object>> minorCreditors = List.of(
            Map.of("company_flag", false) // an individual minor creditor -> should trigger logging
        );

        when(docContext.read("$..minor_creditor")).thenReturn(minorCreditors);

        try (MockedStatic<LogUtil> logUtilMock = Mockito.mockStatic(LogUtil.class)) {
            logUtilMock.when(LogUtil::getIpAddress).thenReturn(expectedIp);
            logUtilMock.when(() -> LogUtil.getCurrentDateTime(clock)).thenReturn(expectedNow);

            // Act - direct call, pass the mocked docContext
            pdplLoggingService.logSubmitDraftAccountMinorCreditorInfo(docContext, entity);

            // Assert
            ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
                ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);

            verify(loggingService, times(1)).personalDataAccessLogAsync(captor.capture());

            PersonalDataProcessingLogDetails captured = captor.getValue();

            assertThat(captured.getBusinessIdentifier()).isEqualTo(expectedBusinessIdentifier);
            assertThat(captured.getIpAddress()).isEqualTo(expectedIp);
            assertThat(captured.getCreatedAt()).isEqualTo(expectedNow);

            assertThat(captured.getCreatedBy().getIdentifier()).isEqualTo(submittedBy);
            assertThat(captured.getIndividuals()).hasSize(1);
            assertThat(captured.getIndividuals().get(0).getIdentifier()).isEqualTo(draftId.toString());
        }
    }

    @Test
    void jsonPathUtil_extracts_values_from_minimal_account_json() {
        // Build a minimal account JSON that satisfies the schema "required" list.
        ObjectMapper om = new ObjectMapper();
        ObjectNode root = om.createObjectNode();

        root.put("account_type", "DEFENDANT");
        root.put("defendant_type", "ADULT");
        root.put("originator_name", "LJS");
        root.put("originator_id", 123);
        root.put("enforcement_court_id", 10);
        root.put("payment_card_request", false);
        root.put("account_sentence_date", "2025-01-01");

        // minimal defendant object (required: company_flag, address_line_1)
        ObjectNode defendant = om.createObjectNode();
        defendant.put("company_flag", false);
        defendant.put("address_line_1", "1 Example Street");
        root.set("defendant", defendant);

        // minimal offences array (each offence requires date_of_sentence, offence_id, impositions)
        ObjectNode imposition0 = om.createObjectNode();
        imposition0.put("result_id", "R1");
        imposition0.put("amount_imposed", 100.00);
        imposition0.put("amount_paid", 0.00);
        ArrayNode impositions = om.createArrayNode().add(imposition0);

        ObjectNode offence0 = om.createObjectNode();
        offence0.put("date_of_sentence", "2024-12-01");
        offence0.put("offence_id", 42);
        offence0.set("impositions", impositions);
        ArrayNode offences = om.createArrayNode().add(offence0);
        root.set("offences", offences);

        // minimal payment_terms (required: payment_terms_type_code)
        ObjectNode paymentTerms = om.createObjectNode();
        paymentTerms.put("payment_terms_type_code", "B");
        root.set("payment_terms", paymentTerms);

        String json = root.toString();

        // Assertions using JsonPath
        assertEquals("1 Example Street",
            com.jayway.jsonpath.JsonPath.read(json, "$.defendant.address_line_1"));

        assertEquals("B",
            com.jayway.jsonpath.JsonPath.read(json, "$.payment_terms.payment_terms_type_code"));

        assertThat(((Number) com.jayway.jsonpath.JsonPath.read(json, "$.offences[0].offence_id"))
            .intValue()).isEqualTo(42);
    }

    @Test
    void jsonPathUtil_handles_arrays_and_missing_optional_fields() {
        // Construct JSON with multiple offences and absent optional fields to ensure JsonPath still works
        ObjectMapper om = new ObjectMapper();
        ObjectNode root = om.createObjectNode();

        root.put("account_type", "DEFENDANT");
        root.put("defendant_type", "ADULT");
        root.put("originator_name", "LJS");
        root.put("originator_id", 123);
        root.put("enforcement_court_id", 10);
        root.put("payment_card_request", true);
        root.put("account_sentence_date", "2025-01-10");

        ObjectNode defendant = om.createObjectNode();
        defendant.put("company_flag", false);
        defendant.put("address_line_1", "4 Another Lane");
        root.set("defendant", defendant);

        // offences
        ObjectNode impositionA = om.createObjectNode();
        impositionA.put("result_id", "R1");
        impositionA.put("amount_imposed", 50.0);
        impositionA.put("amount_paid", 10.0);
        ArrayNode impositionsA = om.createArrayNode().add(impositionA);

        ObjectNode offenceA = om.createObjectNode();
        offenceA.put("date_of_sentence", "2024-10-01");
        offenceA.put("offence_id", 7);
        offenceA.set("impositions", impositionsA);

        ObjectNode offenceB = om.createObjectNode();
        offenceB.put("date_of_sentence", "2024-11-01");
        offenceB.put("offence_id", 8);
        offenceB.set("impositions", impositionsA);

        ArrayNode offences = om.createArrayNode().add(offenceA).add(offenceB);
        root.set("offences", offences);

        // payment_terms
        ObjectNode paymentTerms = om.createObjectNode();
        paymentTerms.put("payment_terms_type_code", "P");
        root.set("payment_terms", paymentTerms);

        String json = root.toString();

        // JsonPath returns list of offence ids
        @SuppressWarnings("unchecked")
        List<Integer> ids = com.jayway.jsonpath.JsonPath.read(json, "$.offences[*].offence_id");
        assertEquals(List.of(7, 8), ids);

        // Missing optional field: assert that JsonPath throws PathNotFoundException for the absent path
        assertThrows(com.jayway.jsonpath.PathNotFoundException.class, () ->
            com.jayway.jsonpath.JsonPath.read(json, "$.nonexistent_field")
        );
    }

    @Test
    @DisplayName("defendant_type == company -> no PDPL logging")
    void pdplForSubmitDraftAccount_company_noLogging() {
        String accountJson = """
            {
              "account_type":"Fines",
              "defendant_type":"company",
              "originator_name":"LJS",
              "originator_id": 1
            }
            """;

        DraftAccountEntity entity = DraftAccountEntity.builder()
            .draftAccountId(1L)
            .submittedBy("user-x")
            .account(accountJson)
            .build();

        pdplLoggingService.pdplForSubmitDraftAccount(entity);

        // no calls expected
        verify(loggingService, times(0)).personalDataAccessLogAsync(any());
    }

    @Test
    @DisplayName("adultOrYouthOnly without minor_creditor -> only Defendant logged")
    void pdplForSubmitDraftAccount_adultNoMinor_onlyDefendant() {
        String accountJson = """
            {
              "account_type":"Fines",
              "defendant_type":"adultOrYouthOnly",
              "originator_name":"LJS",
              "originator_id": 2,
              "offences": []
            }
            """;

        DraftAccountEntity entity = DraftAccountEntity.builder()
            .draftAccountId(11L)
            .submittedBy("user-def")
            .account(accountJson)
            .build();

        try (MockedStatic<LogUtil> logUtil = Mockito.mockStatic(LogUtil.class)) {
            logUtil.when(LogUtil::getIpAddress).thenReturn("10.10.10.10");
            logUtil.when(() -> LogUtil.getCurrentDateTime(clock))
                .thenReturn(OffsetDateTime.parse("2025-02-02T02:02:02Z"));

            pdplLoggingService.pdplForSubmitDraftAccount(entity);
        }

        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);

        verify(loggingService, times(1)).personalDataAccessLogAsync(captor.capture());

        PersonalDataProcessingLogDetails captured = captor.getValue();
        assertThat(captured).isNotNull();
        assertEquals("Submit Draft Account - Defendant", captured.getBusinessIdentifier());
        assertEquals("11", captured.getIndividuals().get(0).getIdentifier());
        assertEquals("user-def", captured.getCreatedBy().getIdentifier());
    }

    @Test
    @DisplayName("adultOrYouthOnly with individual minor_creditor -> Defendant + Minor Creditor logged")
    void pdplForSubmitDraftAccount_adultWithMinor_logsDefendantAndMinor() {
        String accountJson = """
            {
              "account_type":"Fines",
              "defendant_type":"adultOrYouthOnly",
              "originator_name":"LJS",
              "originator_id": 3,
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
            """;

        DraftAccountEntity entity = DraftAccountEntity.builder()
            .draftAccountId(22L)
            .submittedBy("user-min")
            .account(accountJson)
            .build();

        try (MockedStatic<LogUtil> logUtil = Mockito.mockStatic(LogUtil.class)) {
            logUtil.when(LogUtil::getIpAddress).thenReturn("192.0.2.1");
            logUtil.when(() -> LogUtil.getCurrentDateTime(clock))
                .thenReturn(OffsetDateTime.parse("2025-03-03T03:03:03Z"));

            pdplLoggingService.pdplForSubmitDraftAccount(entity);
        }

        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);

        verify(loggingService, times(2)).personalDataAccessLogAsync(captor.capture());

        List<PersonalDataProcessingLogDetails> capturedList = captor.getAllValues();
        assertEquals(2, capturedList.size());

        assertEquals("Submit Draft Account - Defendant", capturedList.get(0).getBusinessIdentifier());
        assertEquals("Submit Draft Account - Minor Creditor", capturedList.get(1).getBusinessIdentifier());
        assertEquals("22", capturedList.get(0).getIndividuals().get(0).getIdentifier());
        assertEquals("user-min", capturedList.get(0).getCreatedBy().getIdentifier());
    }

    @Test
    @DisplayName("pgToPay -> Parent/Guardian and Defendant logged (in that order)")
    void pdplForSubmitDraftAccount_pgToPay_parentThenDefendant() {
        String accountJson = """
            {
              "account_type":"Fines",
              "defendant_type":"pgToPay",
              "originator_name":"LJS",
              "originator_id": 4,
              "offences": []
            }
            """;

        DraftAccountEntity entity = DraftAccountEntity.builder()
            .draftAccountId(33L)
            .submittedBy("user-pg")
            .account(accountJson)
            .build();

        try (MockedStatic<LogUtil> logUtil = Mockito.mockStatic(LogUtil.class)) {
            logUtil.when(LogUtil::getIpAddress).thenReturn("203.0.113.5");
            logUtil.when(() -> LogUtil.getCurrentDateTime(clock))
                .thenReturn(OffsetDateTime.parse("2025-04-04T04:04:04Z"));

            pdplLoggingService.pdplForSubmitDraftAccount(entity);
        }

        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);

        verify(loggingService, times(2)).personalDataAccessLogAsync(captor.capture());

        List<PersonalDataProcessingLogDetails> capturedList = captor.getAllValues();
        assertEquals("Submit Draft Account - Parent or Guardian", capturedList.get(0).getBusinessIdentifier());
        assertEquals("Submit Draft Account - Defendant", capturedList.get(1).getBusinessIdentifier());
        assertEquals("33", capturedList.get(0).getIndividuals().get(0).getIdentifier());
        assertEquals("user-pg", capturedList.get(0).getCreatedBy().getIdentifier());
    }

    @Test
    @DisplayName("logSubmitDraftAccountMinorCreditorInfo - when docContext.read returns null -> no logging")
    void logSubmitDraftAccountMinorCreditorInfo_handlesNullMinorCreditors_noLogging() {
        // Arrange
        DraftAccountEntity entity = Mockito.mock(DraftAccountEntity.class);

        JsonPathUtil.DocContext docContext = Mockito.mock(JsonPathUtil.DocContext.class);
        when(docContext.read("$..minor_creditor")).thenReturn(null);

        // Act
        pdplLoggingService.logSubmitDraftAccountMinorCreditorInfo(docContext, entity);

        // Assert - loggingService should NOT be called
        verify(loggingService, times(0)).personalDataAccessLogAsync(any());
    }

}
