package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.core.JacksonException;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.generated.model.DefendantAccountSearchDefendantDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PostDefendantAccountSearchRequestDefendantAccount;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@DisplayName("Defendant Account API Controller Search Integration Test")
public class DefendantAccountApiControllerSearchIntegrationTest extends AbstractIntegrationTest {

    private static final String DEFENDANT_ACCOUNT_SEARCH_API_URL = "/defendant-accounts/search";

    private static final String AUTHORIZATION_HEADER = "authorization";

    @Test
    @JiraEpic("PO-2630")
    @JiraStory("PO-2970")
    @DisplayName("AC1 - If national insurance number is provided no other fields can be provided")
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
                .nationalInsuranceNumber("QQ123456C")
                .build())
            .activeAccountsOnly(true)
            .businessUnitIds(List.of(101))
            .build();

        ResultActions result = mockMvc.perform(post(DEFENDANT_ACCOUNT_SEARCH_API_URL)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(AUTHORIZATION_HEADER, userStateStub.getBearerToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(searchRequest)));

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

    @Test
    @JiraEpic("PO-2630")
    @JiraStory("PO-2970")
    @DisplayName("AC2 - If national insurance number is provided no other fields can be provided")
    void postDefendantAccountSearch_NI_number_provides_successful_response() throws Exception {
        PostDefendantAccountSearchRequestDefendantAccount searchRequest =
            PostDefendantAccountSearchRequestDefendantAccount.builder()
            .defendant(DefendantAccountSearchDefendantDefendantAccount.builder()
                .nationalInsuranceNumber("QQ123456C")
                .build())
            .activeAccountsOnly(true)
            .businessUnitIds(List.of(101))
            .build();

        ResultActions result = mockMvc.perform(post(DEFENDANT_ACCOUNT_SEARCH_API_URL)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(AUTHORIZATION_HEADER, userStateStub.getBearerToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(searchRequest)));

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").exists())
            .andExpect(jsonPath("$.defendant_accounts").exists());
    }

    @Test
    @JiraEpic("PO-2630")
    @JiraStory("PO-2970")
    @DisplayName("AC3 - If first name is populated last name must also be populated")
    void postDefendantAccountSearch_only_first_name_provided() throws Exception {
        PostDefendantAccountSearchRequestDefendantAccount searchRequest =
            PostDefendantAccountSearchRequestDefendantAccount.builder()
            .defendant(DefendantAccountSearchDefendantDefendantAccount.builder()
                    .forenames("john")
                    .build())
            .activeAccountsOnly(true)
            .businessUnitIds(List.of(101))
            .build();

        ResultActions result = mockMvc.perform(post(DEFENDANT_ACCOUNT_SEARCH_API_URL)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(AUTHORIZATION_HEADER, userStateStub.getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(searchRequest)));

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

    @Test
    @JiraEpic("PO-2630")
    @JiraStory("PO-2970")
    @DisplayName("AC4 - If first name is populated last name must also be populated")
    void postDefendantAccountSearch_first_name_and_last_name_provides_successful_response() throws Exception {
        PostDefendantAccountSearchRequestDefendantAccount searchRequest =
            PostDefendantAccountSearchRequestDefendantAccount.builder()
            .defendant(DefendantAccountSearchDefendantDefendantAccount.builder()
                .forenames("john")
                .surname("doe")
                .build())
            .activeAccountsOnly(true)
            .businessUnitIds(List.of(101))
            .build();

        ResultActions result = mockMvc.perform(post(DEFENDANT_ACCOUNT_SEARCH_API_URL)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(AUTHORIZATION_HEADER, userStateStub.getBearerToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(searchRequest)));

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").exists())
            .andExpect(jsonPath("$.defendant_accounts").exists());
    }

    @Test
    @JiraEpic("PO-2630")
    @JiraStory("PO-2970")
    @DisplayName("AC5 - If Date of birth is populated last name must also be populated")
    void postDefendantAccountSearch_only_has_date_of_brith() throws Exception {
        PostDefendantAccountSearchRequestDefendantAccount searchRequest =
            PostDefendantAccountSearchRequestDefendantAccount.builder()
            .defendant(DefendantAccountSearchDefendantDefendantAccount.builder()
                .birthDate(LocalDate.of(1980, 1, 1))
                .build())
            .activeAccountsOnly(true)
            .businessUnitIds(List.of(101))
            .build();

        ResultActions result = mockMvc.perform(post(DEFENDANT_ACCOUNT_SEARCH_API_URL)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(AUTHORIZATION_HEADER, userStateStub.getBearerToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(searchRequest)));

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

    @Test
    @JiraEpic("PO-2630")
    @JiraStory("PO-2970")
    @DisplayName("AC6 - If Date of birth is populated last name must also be populated")
    void postDefendantAccountSearch_date_of_birth_and_last_name_provide_successful_response() throws Exception {
        PostDefendantAccountSearchRequestDefendantAccount searchRequest =
            PostDefendantAccountSearchRequestDefendantAccount.builder()
            .defendant(DefendantAccountSearchDefendantDefendantAccount.builder()
                .birthDate(LocalDate.of(1980, 1, 1))
                .surname("doe")
                .build())
            .activeAccountsOnly(true)
            .businessUnitIds(List.of(101))
            .build();

        ResultActions result = mockMvc.perform(post(DEFENDANT_ACCOUNT_SEARCH_API_URL)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(AUTHORIZATION_HEADER, userStateStub.getBearerToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(searchRequest)));

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").exists())
            .andExpect(jsonPath("$.defendant_accounts").exists());
    }

    @Test
    @JiraEpic("PO-2630")
    @JiraStory("PO-2970")
    @DisplayName("AC7 - If Date of birth is populated last name must also be populated")
    void postDefendantAccountSearch_request_includes_line1() throws Exception {
        PostDefendantAccountSearchRequestDefendantAccount searchRequest =
            PostDefendantAccountSearchRequestDefendantAccount.builder()
            .defendant(DefendantAccountSearchDefendantDefendantAccount.builder()
                //.includeAliases(false)
                .addressLine1("123 Fake Street")
                //.postcode("SW1A 1AA")
                //.organisationName("org")
                //.exactMatchOrganisationName(false)
                .build())
            .activeAccountsOnly(true)
            .businessUnitIds(List.of(101))
            .build();

        ResultActions result = mockMvc.perform(post(DEFENDANT_ACCOUNT_SEARCH_API_URL)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(AUTHORIZATION_HEADER, userStateStub.getBearerToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(searchRequest)));

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").exists())
            .andExpect(jsonPath("$.defendant_accounts").exists());
    }

    @Test
    @JiraEpic("PO-2630")
    @JiraStory("PO-2970")
    @DisplayName("AC8 - If I only add post code I should not be required to add last name or organisation")
    void postDefendantAccountSearch_request_includes_postcode() throws Exception {
        PostDefendantAccountSearchRequestDefendantAccount searchRequest =
            PostDefendantAccountSearchRequestDefendantAccount.builder()
            .defendant(DefendantAccountSearchDefendantDefendantAccount.builder()
                //.includeAliases(false)
                //.addressLine1("123 Fake Street")
                .postcode("SW1A 1AA")
                //.organisationName("org")
                .build())
            .activeAccountsOnly(true)
            .businessUnitIds(List.of(101))
            .build();

        ResultActions result = mockMvc.perform(post(DEFENDANT_ACCOUNT_SEARCH_API_URL)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(AUTHORIZATION_HEADER, userStateStub.getBearerToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(searchRequest)));

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").exists())
            .andExpect(jsonPath("$.defendant_accounts").exists());
    }

    @Test
    @JiraEpic("PO-2630")
    @JiraStory("PO-2970")
    @DisplayName("AC9 - Surname should be filtered by starts with")
    void postDefendantAccountSearch_surname_filtered_by_starts_with() throws Exception {
        PostDefendantAccountSearchRequestDefendantAccount searchRequest =
            PostDefendantAccountSearchRequestDefendantAccount.builder()
            .defendant(DefendantAccountSearchDefendantDefendantAccount.builder()
                .surname("Smith")
                .build())
            .activeAccountsOnly(true)
            .businessUnitIds(List.of(101))
            .build();

        ResultActions result = mockMvc.perform(post(DEFENDANT_ACCOUNT_SEARCH_API_URL)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(AUTHORIZATION_HEADER, userStateStub.getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(searchRequest)));

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").exists())
            .andExpect(jsonPath("$.defendant_accounts").exists());
    }

    private String toJson(PostDefendantAccountSearchRequestDefendantAccount request) {
        try {
            return ToJsonString.getObjectMapper().writeValueAsString(request);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Invalid JSON data", e);
        }
    }
}
