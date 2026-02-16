package uk.gov.hmcts.opal.controllers;

import jakarta.persistence.QueryTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.OngoingStubbing;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.DraftAccountResponseDto;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitFullEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.service.DraftAccountService;

import java.net.ConnectException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.entity.draft.DraftAccountStatus.SUBMITTED;


@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.DraftAccountControllerTransientErrorsIntegrationTest")
@DisplayName("DraftAccountController Transient Errors Integration Tests")
class DraftAccountControllerTransientErrorsIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/draft-accounts";

    private static final Short BU_ID = (short)007;

    @MockitoBean
    @Autowired
    DraftAccountService draftAccountService;

    @Test
    @DisplayName("Update draft account - Should return 406 Not Acceptable [@PO-973, @PO-747]")
    void testUpdateDraftAccount_trap406Response() throws Exception {
        DraftAccountResponseDto dto = DraftAccountService.toGetResponseDto(createDraftAccountEntity("Test", BU_ID));
        when(draftAccountService.updateDraftAccount(any(), any(), any(), any())).thenReturn(dto);
        shouldReturn406WhenResponseContentTypeNotSupported(
            patch(URL_BASE + "/1").contentType(MediaType.APPLICATION_JSON).content(validUpdateRequestBody())
        );
    }

    @Test
    @DisplayName("Update draft account - Should return 408 [@PO-2117] ")
    void testUpdateDraftAccount_trap408Response() throws Exception {
        shouldReturn408WhenTimeout(
            patch(URL_BASE + "/1")
                .header("If-Match", "0")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validUpdateRequestBody()),
            when(draftAccountService.updateDraftAccount(any(), any(), any(), any()))
        );
    }

    @Test
    @DisplayName("Update draft account - Should return 503 [@PO-2117] ")
    void testUpdateDraftAccount_trap503Response() throws Exception {
        shouldReturn503WhenDownstreamServiceIsUnavailable(
            patch(URL_BASE + "/1")
                .header("If-Match", "0")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validUpdateRequestBody()),
            when(draftAccountService.updateDraftAccount(any(), any(), any(), any()))
        );
    }

    @Test
    @DisplayName("Post draft account - Should return 406 Not Acceptable [@PO-973, @PO-691]")
    void testPostDraftAccount_trap406Response() throws Exception {
        String validRequestBody = validCreateRequestBody();
        shouldReturn406WhenResponseContentTypeNotSupported(
            post(URL_BASE).contentType(MediaType.APPLICATION_JSON).content(validRequestBody));
    }

    @Test
    void testPostDraftAccount_trap408Response() throws Exception {
        String validRequestBody = validCreateRequestBody();
        shouldReturn408WhenTimeout(
            post(URL_BASE).contentType(MediaType.APPLICATION_JSON).content(validRequestBody),
            when(draftAccountService.submitDraftAccount(any(), any())));
    }

    @Test
    void testPostDraftAccount_trap503Response() throws Exception {
        String validRequestBody = validCreateRequestBody();
        shouldReturn503WhenDownstreamServiceIsUnavailable(
            post(URL_BASE).contentType(MediaType.APPLICATION_JSON).content(validRequestBody),
            when(draftAccountService.submitDraftAccount(any(), any()))
        );

    }

    @Test
    void testGetDraftAccountById_trap408Response() throws Exception {
        shouldReturn408WhenTimeout(
            get(URL_BASE + "/1"), when(
                draftAccountService.getDraftAccount(1L, "Bearer some_value")));
    }

    @Test
    void testGetDraftAccountById_trap503Response() throws Exception {
        shouldReturn503WhenDownstreamServiceIsUnavailable(
            get(URL_BASE + "/1"), when(
                draftAccountService.getDraftAccount(1L, "Bearer some_value")));
    }



    @Test
    @DisplayName("Get draft account summaries - Should return 406 Not Acceptable [@PO-973, @PO-647]")
    void testGetDraftAccountsSummaries_trap406Response() throws Exception {
        shouldReturn406WhenResponseContentTypeNotSupported(get(URL_BASE));
    }

    @Test
    void testGetDraftAccountsSummaries_trap408Response() throws Exception {
        shouldReturn408WhenTimeout(
            get(URL_BASE), when(draftAccountService
                                    .getDraftAccounts(any(), any(), any(), any(), any(), any(), any())));
    }

    @Test
    void testGetDraftAccountsSummaries_trap503Response() throws Exception {
        shouldReturn503WhenDownstreamServiceIsUnavailable(
            get(URL_BASE), when(draftAccountService
                                    .getDraftAccounts(any(), any(), any(), any(), any(), any(), any())));
    }

    void shouldReturn406WhenResponseContentTypeNotSupported(MockHttpServletRequestBuilder reqBuilder) throws Exception {
        mockMvc.perform(reqBuilder
                            .header("Authorization", "Bearer " + "some_value")
                            .accept("application/xml"))
            .andExpect(status().isNotAcceptable());
    }


    void shouldReturn408WhenTimeout(MockHttpServletRequestBuilder reqBuilder, OngoingStubbing<?> stubbing)
        throws Exception {
        // Simulating a timeout exception when the service is called
        stubbing.thenThrow(new QueryTimeoutException());

        ResultActions actions = mockMvc.perform(
            reqBuilder.header("Authorization", "Bearer " + "some_value"));
        String response = actions.andReturn().getResponse().getContentAsString();
        log.info(":shouldReturn408WhenTimeout: response: \n{}", ToJsonString.toPrettyJson(response));

        actions
            .andExpect(status().isRequestTimeout())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(content().json("""
            {
                "title": "Request Timeout",
                "detail": "The request did not receive a response from the database within the timeout period",
                "status": 408,
                "type": "https://hmcts.gov.uk/problems/query-timeout"
            }"""));
    }


    void shouldReturn503WhenDownstreamServiceIsUnavailable(MockHttpServletRequestBuilder reqBuilder,
                                                           OngoingStubbing<?> stubbing) throws Exception {
        stubbing.thenAnswer(
            invocation -> {
                throw new PSQLException("Connection refused", PSQLState.CONNECTION_FAILURE, new ConnectException());
            });

        mockMvc.perform(reqBuilder.header("Authorization", "Bearer " + "some_value"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(content().json("""
            {
                "title": "Service Unavailable",
                "detail": "Opal Fines Database is currently unavailable",
                "status": 503,
                "type": "https://hmcts.gov.uk/problems/database-unavailable"
            }"""));
    }


    private static String validCreateRequestBody() {
        return """
            {
              "business_unit_id": 77,
              "submitted_by": "BUUID1",
              "submitted_by_name": "John",
              "account": {
              "account_type": "Fine",
              "defendant_type": "Adult",
              "originator_name": "Police Force",
              "originator_id": 12345,
              "originator_type": "NEW",
              "enforcement_court_id": 101,
              "collection_order_made": true,
              "collection_order_made_today": false,
              "payment_card_request": true,
              "account_sentence_date": "2023-12-01",
              "defendant": {
                "company_flag": false,
                "title": "Mr",
                "surname": "LNAME",
                "forenames": "John",
                "dob": "1985-04-15",
                "address_line_1": "123 Elm Street",
                "address_line_2": "Suite 45",
                "post_code": "AB1 2CD",
                "telephone_number_home": "0123456789",
                "telephone_number_mobile": "07712345678",
                "email_address_1": "john.doe@example.com",
                "national_insurance_number": "AB123456C",
                "nationality_1": "British",
                "occupation": "Engineer",
                "debtor_detail": {
                  "document_language": "English",
                  "hearing_language": "English",
                  "vehicle_make": "Toyota",
                  "vehicle_registration_mark": "ABC123",
                  "aliases": [
                    {
                      "alias_forenames": "Jon",
                      "alias_surname": "Smith"
                    }
                  ]
                }
              },
              "offences": [
                {
                  "date_of_sentence": "2023-11-15",
                  "imposing_court_id": 202,
                  "offence_id": 1234,
                  "impositions": [
                    {
                      "result_id": "1",
                      "amount_imposed": 500.00,
                      "amount_paid": 200.00,
                      "major_creditor_id": 999
                    }
                  ]
                }
              ],
              "payment_terms": {
                "payment_terms_type_code": "P",
                "effective_date": "2023-11-01",
                "instalment_period": "M",
                "lump_sum_amount": 1000.00,
                "instalment_amount": 200.00,
                "default_days_in_jail": 5
              },
              "account_notes": [
                {
                  "account_note_serial": 1,
                  "account_note_text": "Defendant requested an installment plan.",
                  "note_type": "AC"
                }
              ]
            }
            ,
              "account_type": "Fines",
              "account_status": "Submitted",
              "timeline_data": [
                    {
                        "username": "johndoe123",
                        "status": "Active",
                        "status_date": "2023-11-01",
                        "reason_text": "Account successfully activated after review."
                    },
                    {
                        "username": "janedoe456",
                        "status": "Pending",
                        "status_date": "2023-12-05",
                        "reason_text": "Awaiting additional documentation for verification."
                    },
                    {
                        "username": "mikebrown789",
                        "status": "Suspended",
                        "status_date": "2023-10-15",
                        "reason_text": "Violation of terms of service."
                    }
                ]
            }""";
    }

    private static String invalidCreateRequestBody() {
        return """
{
    "invalid_field": "This field shouldn't be here",
    "account": {
        "account_create_request": {
            "defendant": {
                "company_name": "Company ABC",
                "surname": "LNAME",
                "fornames": "FNAME",
                "dob": "2000-01-01"
            },
            "account": {
                "account_type": "Invalid"
            }
        }
    },
    "business_unit_id": 1
}""";
    }

    private static String validUpdateRequestBody() {
        return "{\n"
            + "    \"account_status\": \"PENDING\",\n"
            + "    \"validated_by\": \"BUUID1\",\n"
            + "    \"business_unit_id\": 5,\n"
            + "    \"timeline_data\": " + validTimelineDataJson() + "\n"
            + "}";
    }

    private static String validTimelineDataJson() {
        return """
            [
                {
                    "username": "johndoe123",
                    "status": "Active",
                    "status_date": "2023-11-01",
                    "reason_text": "Account successfully activated after review."
                },
                {
                    "username": "janedoe456",
                    "status": "Pending",
                    "status_date": "2023-12-05",
                    "reason_text": "Awaiting additional documentation for verification."
                },
                {
                    "username": "mikebrown789",
                    "status": "Suspended",
                    "status_date": "2023-10-15",
                    "reason_text": "Violation of terms of service."
                }
            ]""";
    }

    private final String validAccountJson() {
        return """
            {
              "account_type": "Fine",
              "defendant_type": "Adult",
              "originator_name": "Police Force",
              "originator_id": 12345,
              "enforcement_court_id": 101,
              "collection_order_made": true,
              "collection_order_made_today": false,
              "payment_card_request": true,
              "account_sentence_date": "2023-12-01",
              "defendant": {
                "company_flag": true,
                "company_name": "company",
                "dob": "1985-04-15",
                "address_line_1": "123 Elm Street",
                "address_line_2": "Suite 45",
                "post_code": "AB1 2CD",
                "telephone_number_home": "0123456789",
                "telephone_number_mobile": "07712345678",
                "email_address_1": "john.doe@example.com",
                "national_insurance_number": "AB123456C",
                "nationality_1": "British",
                "occupation": "Engineer",
                "debtor_detail": {
                  "document_language": "English",
                  "hearing_language": "English",
                  "vehicle_make": "Toyota",
                  "vehicle_registration_mark": "ABC123",
                  "aliases": [
                    {
                      "alias_forenames": "Jon",
                      "alias_surname": "Smith"
                    }
                  ]
                }
              },
              "offences": [
                {
                  "date_of_sentence": "2023-11-15",
                  "imposing_court_id": 202,
                  "offence_id": 1234,
                  "impositions": [
                    {
                      "result_id": "1",
                      "amount_imposed": 500.00,
                      "amount_paid": 200.00,
                      "major_creditor_id": 999
                    }
                  ]
                }
              ],
              "payment_terms": {
                "payment_terms_type_code": "P",
                "effective_date": "2023-11-01",
                "instalment_period": "M",
                "lump_sum_amount": 1000.00,
                "instalment_amount": 200.00,
                "default_days_in_jail": 5
              },
              "account_notes": [
                {
                  "account_note_serial": 1,
                  "account_note_text": "Defendant requested an installment plan.",
                  "note_type": "AC"
                }
              ]
            }""";
    }

    private DraftAccountEntity createDraftAccountEntity(String submittedBy, short businessUnit) {
        return DraftAccountEntity.builder()
            .draftAccountId(1L)
            .businessUnit(BusinessUnitFullEntity.builder().businessUnitId(businessUnit).build())
            .createdDate(LocalDate.of(2023, 1, 2).atStartOfDay())
            .submittedBy(submittedBy)
            .accountType("DRAFT")
            .accountStatus(SUBMITTED)
            .statusMessage("Status is OK")
            .accountStatusDate(LocalDateTime.of(2024, 11, 11, 11, 11))
            .account(validAccountJson())
            .accountSnapshot("{ \"data\": \"something snappy\"}")
            .timelineData(validTimelineDataJson())
            .build();
    }
}
