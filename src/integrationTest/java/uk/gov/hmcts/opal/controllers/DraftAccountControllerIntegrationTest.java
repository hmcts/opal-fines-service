package uk.gov.hmcts.opal.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allFinesPermissionUser;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.noFinesPermissionUser;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.permissionUser;

import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Slf4j(topic = "opal.DraftAccountControllerIntegrationTest00")
@DisplayName("DraftAccountController Integration Tests")
class DraftAccountControllerIntegrationTest extends CommonDraftAccountControllerIntegrationTest {

    @Test
    @DisplayName("Search draft accounts - POST with draftAccountId - Should return matching draft account"
        + " [@PO-973, @PO-559]")
    void testSearchDraftAccountsPost() throws Exception {

        ResultActions resultActions = mockMvc.perform(post(URL_BASE + "/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"draftAccountId\":\"1\"}"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testSearchDraftAccountsPost: body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].draft_account_id").value(1))
            .andExpect(jsonPath("$[0].business_unit_id").value(77));

    }


    @Test
    void testSearchDraftAccountsPost_whenDraftAccountDoesNotExist() throws Exception {
        ResultActions resultActions = mockMvc.perform(post(URL_BASE + "/search")
                                          .header("authorization", "Bearer some_value")
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content("{\"draftAccountId\":\"999999\"}"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testSearchDraftAccountsPost_whenDraftAccountDoesNotExist: body:\n"
            + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Delete draft accounts [@PO-973, @PO-591]")
    void testDeleteDraftAccountById_success() throws Exception {

        ResultActions resultActions = mockMvc.perform(delete(URL_BASE + "/4")
            .header("If-Match", "0")
            .header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testDeleteDraftAccountById_success: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Draft Account '4' deleted"));
    }

    private static String validAccountJsonString() {
        return """
            {
              "account_type": "Fine",
              "defendant_type": "Adult",
              "originator_name": "Police Force",
              "originator_id": 12345,
              "originator_type": "NEW",
              "enforcement_court_id": 101,
              "payment_card_request": true,
              "account_sentence_date": "2023-12-01",
              "defendant": {
                "company_flag": false,
                "surname": "LNAME",
                "address_line_1": "123 Main Street"
              },
              "offences": [],
              "payment_terms": {
                "payment_terms_type_code": "P",
                "effective_date": "2023-11-01",
                "instalment_period": "M",
                "lump_sum_amount": 1000.00,
                "instalment_amount": 200.00,
                "default_days_in_jail": 5
              }
            }
            """;
    }

    private static String validTimelineDataString() {
        return """
            [
              {
                "username": "johndoe123",
                "status": "Active",
                "status_date": "2023-11-01",
                "reason_text": "Valid reason"
              }
            ]
            """;
    }


    @Test
    @DisplayName("Should ignore blank submitted_by_name")
    void shouldIgnoreBlankSubmittedByName() throws Exception {
        String request = validCreateRequestBody()
            .replace("\"submitted_by_name\": \"John\"", "\"submitted_by_name\": \"\"");

        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(permissionUser((short) 78, FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS));

        mockMvc.perform(post(URL_BASE)
                .header("Authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should ignore blank submitted_by")
    void shouldIgnoreBlankSubmittedBy() throws Exception {
        String request = validCreateRequestBody()
            .replace("\"submitted_by\": \"BUUID1\"", "\"submitted_by\": \"\"");

        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(permissionUser((short) 78, FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS));

        mockMvc.perform(post(URL_BASE)
                .header("Authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should return 400 when account_type is blank")
    void shouldReturn400WhenAccountTypeIsBlank() throws Exception {
        String request = validCreateRequestBody()
            .replace("\"account_type\": \"Fines\"", "\"account_type\": \"\"");

        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(permissionUser((short) 78, FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS));

        mockMvc.perform(post(URL_BASE)
                .header("Authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when originator_type is missing")
    void shouldReturn400WhenOriginatorTypeIsMissing() throws Exception {
        String request = validCreateRequestBody()
            .replace("\"originator_type\": \"NEW\",", "");

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        mockMvc.perform(post(URL_BASE)
                .header("Authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when originator_type is blank")
    void shouldReturn400WhenOriginatorTypeIsBlank() throws Exception {
        String request = validCreateRequestBody()
            .replace("\"originator_type\": \"NEW\"", "\"originator_type\": \"\"");

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        mockMvc.perform(post(URL_BASE)
                .header("Authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when originator_type has invalid value")
    void shouldReturn400WhenOriginatorTypeIsInvalid() throws Exception {
        String request = validCreateRequestBody()
            .replace("\"originator_type\": \"NEW\"", "\"originator_type\": \"ABC\"");

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        mockMvc.perform(post(URL_BASE)
                .header("Authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());
    }

    //CEP 1 CEP1 - Invalid Request Payload (400)
    @ParameterizedTest
    @MethodSource("endpointsWithInvalidBodiesProvider")
    void methodsShouldReturn400_whenRequestPayloadIsInvalid(
        MockHttpServletRequestBuilder requestBuilder, String requestBody) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(
            permissionUser((short)78,
                           FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS,
                           FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS));

        mockMvc.perform(requestBuilder
                .header("Authorization", "Bearer some_value")
                .header("Accept", "application/json")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> endpointsWithInvalidBodiesProvider() {
        return Stream.of(Arguments.of(post(URL_BASE), invalidCreateRequestBody()),
            Arguments.of(put(URL_BASE + "/1"), invalidCreateRequestBody()),
            Arguments.of(patch(URL_BASE + "/1"), invalidCreateRequestBody())
        );
    }

    //CEP3 - Not Authorised to perform the requested action (403)
    @ParameterizedTest
    @MethodSource("testCasesRequiringAuthorizationProvider")
    void methodsShouldReturn403_whenUserLacksPermission(
        MockHttpServletRequestBuilder requestBuilder, String requestBody) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(noFinesPermissionUser());

        ResultActions resultActions = mockMvc.perform(requestBuilder
            .header("Authorization", "Bearer some_value")
            .header("If-Match", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":methodsShouldReturn403_whenUserLacksPermission: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"));
    }

    private static Stream<Arguments> testCasesRequiringAuthorizationProvider() {
        return Stream.of(
            Arguments.of(post(URL_BASE), validCreateRequestBody()),
            Arguments.of(put(URL_BASE + "/1"), validCreateRequestBody()),
            Arguments.of(patch(URL_BASE + "/1"), validUpdateRequestBody("78", "Publishing Pending","B")),
            Arguments.of(get(URL_BASE), "")  // GET endpoints with empty body
        );
    }

    //CEP4 - Resource Not Found (404) - applies to GET PUT PATCH & DELETE
    @ParameterizedTest
    @MethodSource("testCasesForResourceNotFoundProvider")
    void methodsShouldReturn404_whenResourceNotFound(
        MockHttpServletRequestBuilder requestBuilder, String requestBody) throws Exception {
        // Set up a non-existent ID
        long nonExistentId = 999L;

        // Mock the service behavior
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(
            permissionUser((short)78,
                           FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS,
                           FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS));

        mockMvc.perform(requestBuilder
                .header("Authorization", "Bearer some_value")
                .header("If-Match", "0")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isNotFound());
    }

    private static Stream<Arguments> testCasesForResourceNotFoundProvider() {
        return Stream.of(
            Arguments.of(get(URL_BASE + "/999"), ""),
            Arguments.of(put(URL_BASE + "/999"), validCreateRequestBody()),
            Arguments.of(patch(URL_BASE + "/999"), validUpdateRequestBody("78", "Publishing Pending","C"))
        );
    }

    //CEP5 - Unsupported Content Type for Response (406)
    @ParameterizedTest
    @MethodSource("testCasesWithValidBodiesProvider")
    void methodsShouldReturn406_whenAcceptHeaderIsNotSupported(
        MockHttpServletRequestBuilder requestBuilder, String requestBody) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(
            permissionUser((short)78,
                           FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS,
                           FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS));
        mockMvc.perform(requestBuilder
                .header("Authorization", "Bearer some_value")
                .header("Accept", "application/xml")
                .header("If-Match", "0")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isNotAcceptable());
    }

    private static Stream<Arguments> testCasesWithValidBodiesProvider() {
        return Stream.of(Arguments.of(post(URL_BASE), validCreateRequestBody()),
            Arguments.of(put(URL_BASE + "/1"), "{}"),
            Arguments.of(patch(URL_BASE + "/1"), "{}"),
            Arguments.of(get(URL_BASE + "/1"), "{}")
        );
    }

    private static String validCreateRequestBody() {
        return """
            {
              "business_unit_id": 78,
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
              },
              "account_type": "Fines",
              "account_status": "Submitted",
              "version": 0,
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
}
