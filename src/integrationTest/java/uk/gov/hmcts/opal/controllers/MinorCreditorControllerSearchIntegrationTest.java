package uk.gov.hmcts.opal.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.testutil.JsonErrorAssertions.expectBadRequest;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.Creditor;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@Sql(
    executionPhase = BEFORE_TEST_METHOD,
    scripts = { "classpath:db/insertData/insert_into_minor_creditors.sql" }
)
@Sql(
    executionPhase = AFTER_TEST_METHOD,
    scripts = { "classpath:db/deleteData/delete_from_minor_creditors.sql" }
)
@DisplayName("Minor Creditor Controller Search Integration Test")
public class MinorCreditorControllerSearchIntegrationTest extends AbstractIntegrationTest {

    private static final String MINOR_CREDITOR_SEARCH_URL = "/minor-creditor-accounts/search";

    private static final String AUTHORISATION_HEADER = "authorization";

    @Test
    @JiraEpic("PO-2630")
    @JiraStory("PO-2971")
    @DisplayName("AC01 - If account number is populated no other fields can be - should error")
    void postMinorCreditorAccountSearch_withAccountNumberAndOtherFields_shouldFail() throws Exception {
        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .activeAccountsOnly(true)
            .accountNumber("JS987654")
            .businessUnitIds(List.of((short) 10))
            .creditor(Creditor.builder()
                .addressLine1("44 Hold St.")
                .postcode("DE1 2DE")
                .organisationName("Tech Solutions")
                .exactMatchOrganisationName(false)
                .forenames("John")
                .surname("Smith")
                .exactMatchSurname(false)
                .exactMatchForenames(false)
                .organisation(false)
                .build())
            .build();

        ResultActions result = mockMvc.perform(post(MINOR_CREDITOR_SEARCH_URL)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(AUTHORISATION_HEADER, userStateStub.getBearerToken())
            .contentType(APPLICATION_JSON).content(search.toJson()));

        expectErrorResponse(result);
    }

    @Test
    @JiraEpic("PO-2630")
    @JiraStory("PO-2971")
    @DisplayName("AC02 - If account number is populated no other fields can be - should be successful")
    void postMinorCreditorAccountSearch_withAccountNumber_shouldPass() throws Exception {
        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of((short) 10))
            .activeAccountsOnly(true)
            .accountNumber("JS987654")
            .build();

        ResultActions result = mockMvc.perform(post(MINOR_CREDITOR_SEARCH_URL)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(AUTHORISATION_HEADER, userStateStub.getBearerToken())
            .contentType(APPLICATION_JSON).content(search.toJson()));

        expectOkResponse(result);
    }

    @Test
    @JiraEpic("PO-2630")
    @JiraStory("PO-2971")
    @DisplayName("AC03 - If first name is populated last name must also be populated - should error")
    void postMinorCreditorAccountSearch_withFirstNameWithoutLastName_shouldFail() throws Exception {
        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of((short) 10))
            .activeAccountsOnly(true)
            .creditor(Creditor.builder()
                .forenames("John")
                .exactMatchForenames(false)
                .organisation(false)
                .build())
            .build();

        ResultActions result = mockMvc.perform(post(MINOR_CREDITOR_SEARCH_URL)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(AUTHORISATION_HEADER, userStateStub.getBearerToken())
            .contentType(APPLICATION_JSON).content(search.toJson()));

        expectErrorResponse(result);
    }

    @Test
    @JiraEpic("PO-2630")
    @JiraStory("PO-2971")
    @DisplayName("AC04 - If first name is populated last name must also be populated - should be successful")
    void postMinorCreditorAccountSearch_withFirstNameAndLastName_shouldPass() throws Exception {
        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of((short) 10))
            .activeAccountsOnly(true)
            .creditor(Creditor.builder()
                .forenames("John")
                .surname("Smith")
                .exactMatchForenames(false)
                .exactMatchSurname(false)
                .organisation(false)
                .build())
            .build();

        ResultActions result = mockMvc.perform(post(MINOR_CREDITOR_SEARCH_URL)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(AUTHORISATION_HEADER, userStateStub.getBearerToken())
                .contentType(APPLICATION_JSON).content(search.toJson()));

        expectOkResponse(result);
    }

    private void expectOkResponse(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
            .andExpect(header().exists("operation_id"))
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.creditor_accounts[0].creditor_account_id").value("999950"))
            .andExpect(jsonPath("$.creditor_accounts[0].account_number").value("JS987654"))
            .andExpect(jsonPath("$.creditor_accounts[0].business_unit_id").value("10"))
            .andExpect(jsonPath("$.creditor_accounts[0].firstnames").value("John"))
            .andExpect(jsonPath("$.creditor_accounts[0].surname").value("Smith"));
    }

    private void expectErrorResponse(ResultActions result) throws Exception {
        result.andExpect(status().isBadRequest())
            .andExpect(header().exists("operation_id"))
            .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(expectBadRequest(
                "The request does not conform to the required JSON schema",
                "https://hmcts.gov.uk/problems/json-schema-validation"))
            .andExpect(jsonPath("$.instance").exists())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.operation_id").exists())
            .andExpect(jsonPath("$.retriable").value(false));
    }
}
