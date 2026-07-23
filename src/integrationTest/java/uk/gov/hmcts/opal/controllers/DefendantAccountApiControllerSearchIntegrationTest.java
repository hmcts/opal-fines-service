package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.core.JacksonException;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.generated.model.DefendantAccountSearchDefendantDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PostDefendantAccountSearchRequestDefendantAccount;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@Sql(
    executionPhase = BEFORE_TEST_METHOD,
    scripts = {"classpath:db/insertData/insert_into_defendant_accounts.sql"}
)
@Sql(
    executionPhase = AFTER_TEST_METHOD,
    scripts = {"classpath:db/deleteData/delete_from_defendant_accounts.sql"}
)
@DisplayName("Defendant Accounts Search Controller Integration Tests")
public class DefendantAccountApiControllerSearchIntegrationTest extends AbstractIntegrationTest {

    private static final String DEFENDANT_ACCOUNT_SEARCH_API_URL = "/defendant-accounts/search";

    private static final String AUTHORIZATION_HEADER = "authorization";

    @Test
    @JiraEpic("PO-2630")
    @JiraStory("PO-2970")
    @DisplayName("AC01 - If national insurance number is provided no other fields can be provided")
    void postDefendantAccountSearch_other_fields_cannot_be_provided_when_NI_number_is_provided() throws Exception {
        PostDefendantAccountSearchRequestDefendantAccount searchRequest =
            PostDefendantAccountSearchRequestDefendantAccount.builder()
            .defendant(DefendantAccountSearchDefendantDefendantAccount.builder()
                .includeAliases(false)
                .organisation(false)
                .addressLine1("123 Fake Street")
                .postcode("SW1A 1AA")
                .organisationName("org")
                .exactMatchOrganisationName(false)
                .surname("doe")
                .exactMatchSurname(false)
                .forenames("john")
                .exactMatchForenames(false)
                .birthDate(LocalDate.of(1980, 1, 1))
                .nationalInsuranceNumber("NI2221C")
                .build())
            .activeAccountsOnly(true)
            .businessUnitIds(List.of(78))
            .build();

        ResultActions result = mockMvc.perform(post(DEFENDANT_ACCOUNT_SEARCH_API_URL)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(AUTHORIZATION_HEADER, userStateStub.getBearerToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(searchRequest)));

        expectErrorResultActions(result);
    }

    @Test
    @JiraEpic("PO-2630")
    @JiraStory("PO-2970")
    @DisplayName("AC02 - If national insurance number is provided no other fields can be provided")
    void postDefendantAccountSearch_NI_number_provides_successful_response() throws Exception {
        PostDefendantAccountSearchRequestDefendantAccount searchRequest =
            PostDefendantAccountSearchRequestDefendantAccount.builder()
            .defendant(DefendantAccountSearchDefendantDefendantAccount.builder()
                .nationalInsuranceNumber("NI2221C")
                .build())
            .activeAccountsOnly(true)
            .businessUnitIds(List.of(78))
            .build();

        ResultActions result = mockMvc.perform(post(DEFENDANT_ACCOUNT_SEARCH_API_URL)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(AUTHORIZATION_HEADER, userStateStub.getBearerToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(searchRequest)));

        expectOkResultActions(result);
    }

    @Test
    @JiraEpic("PO-2630")
    @JiraStory("PO-2970")
    @DisplayName("AC03 - If first name is populated last name must also be populated")
    void postDefendantAccountSearch_only_first_name_provided() throws Exception {
        PostDefendantAccountSearchRequestDefendantAccount searchRequest =
            PostDefendantAccountSearchRequestDefendantAccount.builder()
            .defendant(DefendantAccountSearchDefendantDefendantAccount.builder()
                    .forenames("forenamey")
                    .build())
            .activeAccountsOnly(true)
            .businessUnitIds(List.of(78))
            .build();

        ResultActions result = mockMvc.perform(post(DEFENDANT_ACCOUNT_SEARCH_API_URL)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(AUTHORIZATION_HEADER, userStateStub.getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(searchRequest)));

        expectErrorResultActions(result);
    }

    @Test
    @JiraEpic("PO-2630")
    @JiraStory("PO-2970")
    @DisplayName("AC04 - If first name is populated last name must also be populated")
    void postDefendantAccountSearch_first_name_and_last_name_provides_successful_response() throws Exception {
        PostDefendantAccountSearchRequestDefendantAccount searchRequest =
            PostDefendantAccountSearchRequestDefendantAccount.builder()
            .defendant(DefendantAccountSearchDefendantDefendantAccount.builder()
                .forenames("Forenamey")
                .surname("Surnamey")
                .build())
            .activeAccountsOnly(true)
            .businessUnitIds(List.of(78))
            .build();

        ResultActions result = mockMvc.perform(post(DEFENDANT_ACCOUNT_SEARCH_API_URL)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(AUTHORIZATION_HEADER, userStateStub.getBearerToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(searchRequest)));

        expectOkResultActions(result);
    }

    @Test
    @JiraEpic("PO-2630")
    @JiraStory("PO-2970")
    @DisplayName("AC05 - If Date of birth is populated last name must also be populated")
    void postDefendantAccountSearch_only_has_date_of_brith() throws Exception {
        PostDefendantAccountSearchRequestDefendantAccount searchRequest =
            PostDefendantAccountSearchRequestDefendantAccount.builder()
            .defendant(DefendantAccountSearchDefendantDefendantAccount.builder()
                .birthDate(LocalDate.of(1980, 2, 3))
                .build())
            .activeAccountsOnly(true)
            .businessUnitIds(List.of(78))
            .build();

        ResultActions result = mockMvc.perform(post(DEFENDANT_ACCOUNT_SEARCH_API_URL)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(AUTHORIZATION_HEADER, userStateStub.getBearerToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(searchRequest)));

        expectErrorResultActions(result);
    }

    @Test
    @JiraEpic("PO-2630")
    @JiraStory("PO-2970")
    @DisplayName("AC06 - If Date of birth is populated last name must also be populated")
    void postDefendantAccountSearch_date_of_birth_and_last_name_provide_successful_response() throws Exception {
        PostDefendantAccountSearchRequestDefendantAccount searchRequest =
            PostDefendantAccountSearchRequestDefendantAccount.builder()
            .defendant(DefendantAccountSearchDefendantDefendantAccount.builder()
                .birthDate(LocalDate.of(1980, 2, 3))
                .surname("Surnamey")
                .build())
            .activeAccountsOnly(true)
            .businessUnitIds(List.of(78))
            .build();

        ResultActions result = mockMvc.perform(post(DEFENDANT_ACCOUNT_SEARCH_API_URL)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(AUTHORIZATION_HEADER, userStateStub.getBearerToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(searchRequest)));

        expectOkResultActions(result);
    }

    @Test
    @JiraEpic("PO-2630")
    @JiraStory("PO-2970")
    @DisplayName("AC07 - If Date of birth is populated last name must also be populated")
    void postDefendantAccountSearch_request_includes_line1() throws Exception {
        PostDefendantAccountSearchRequestDefendantAccount searchRequest =
            PostDefendantAccountSearchRequestDefendantAccount.builder()
            .defendant(DefendantAccountSearchDefendantDefendantAccount.builder()
                .addressLine1("Square House")
                .build())
            .activeAccountsOnly(true)
            .businessUnitIds(List.of(78))
            .build();

        ResultActions result = mockMvc.perform(post(DEFENDANT_ACCOUNT_SEARCH_API_URL)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(AUTHORIZATION_HEADER, userStateStub.getBearerToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(searchRequest)));

        expectOkResultActions(result);
    }

    @Test
    @JiraEpic("PO-2630")
    @JiraStory("PO-2970")
    @DisplayName("AC08 - If I only add post code I should not be required to add last name or organisation")
    void postDefendantAccountSearch_request_includes_postcode() throws Exception {
        PostDefendantAccountSearchRequestDefendantAccount searchRequest =
            PostDefendantAccountSearchRequestDefendantAccount.builder()
            .defendant(DefendantAccountSearchDefendantDefendantAccount.builder()
                .postcode("BH13 1PO")
                .build())
            .activeAccountsOnly(true)
            .businessUnitIds(List.of(78))
            .build();

        ResultActions result = mockMvc.perform(post(DEFENDANT_ACCOUNT_SEARCH_API_URL)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(AUTHORIZATION_HEADER, userStateStub.getBearerToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(searchRequest)));

        expectOkResultActions(result);
    }

    @Test
    @JiraEpic("PO-2630")
    @JiraStory("PO-2970")
    @DisplayName("AC09 - Surname should be filtered by starts with")
    void postDefendantAccountSearch_surname_filtered_by_starts_with() throws Exception {
        PostDefendantAccountSearchRequestDefendantAccount searchRequest =
            PostDefendantAccountSearchRequestDefendantAccount.builder()
            .defendant(DefendantAccountSearchDefendantDefendantAccount.builder()
                .surname("Sur")
                .build())
            .activeAccountsOnly(true)
            .businessUnitIds(List.of(78))
            .build();

        ResultActions result = mockMvc.perform(post(DEFENDANT_ACCOUNT_SEARCH_API_URL)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(AUTHORIZATION_HEADER, userStateStub.getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(searchRequest)));

        expectOkResultActions(result);
    }

    @Test
    @JiraEpic("PO-2630")
    @JiraStory("PO-2970")
    @DisplayName("AC10 - Returns an empty response when no results match the search criteria")
    void postDefendantAccountSearch_returns_empty_response() throws Exception {
        PostDefendantAccountSearchRequestDefendantAccount searchRequest =
            PostDefendantAccountSearchRequestDefendantAccount.builder()
            .defendant(DefendantAccountSearchDefendantDefendantAccount.builder()
                .nationalInsuranceNumber("QQ123456C")
                .build())
            .activeAccountsOnly(true)
            .businessUnitIds(List.of(1101))
            .build();

        ResultActions result = mockMvc.perform(post(DEFENDANT_ACCOUNT_SEARCH_API_URL)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(AUTHORIZATION_HEADER, userStateStub.getBearerToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(searchRequest)));

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(0))
            .andExpect(jsonPath("$.defendant_accounts").isEmpty());
    }

    private void expectOkResultActions(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id")
                .value("991199"))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id")
                .value("78"))
            .andExpect(jsonPath("$.defendant_accounts[0].account_number")
                .value("1989"))
            .andExpect(jsonPath("$.defendant_accounts[1].defendant_account_id")
            .value("991198"))
            .andExpect(jsonPath("$.defendant_accounts[1].business_unit_id")
                .value("78"))
            .andExpect(jsonPath("$.defendant_accounts[1].account_number")
                .value("1988"))
            .andDo(print());
    }

    private void expectErrorResultActions(ResultActions result) throws Exception {
        result.andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.detail")
                .value("The request does not conform to the required JSON schema"))
            .andExpect(jsonPath("$.instance").exists())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.type")
                .value("https://hmcts.gov.uk/problems/json-schema-validation"))
            .andExpect(jsonPath("$.operation_id").exists())
            .andExpect(jsonPath("$.retriable").value(false));
    }

    private String toJson(PostDefendantAccountSearchRequestDefendantAccount request) {
        try {
            return ToJsonString.getObjectMapper().writeValueAsString(request);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Invalid JSON data", e);
        }
    }
}
