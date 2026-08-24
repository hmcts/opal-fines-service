package uk.gov.hmcts.opal.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration"})
@TestPropertySource(properties = {"launchdarkly.enabled=false", "launchdarkly.default-flag-values.release-1b=false"})
@DisplayName("Major Creditor Controller Release 1B Disabled Integration Test")
@Sql(scripts = "classpath:db/insertData/insert_into_creditor_accounts.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_creditor_accounts.sql", executionPhase = AFTER_TEST_METHOD)
public class MajorCreditorControllerRelease1bDisabledIntegrationTest extends AbstractIntegrationTest {

    private static final String MAJOR_CREDITORS_URL = "/major-creditors";

    private static final String AUTHORISATION_HEADER = "authorization";

    private static final String GET_MAJOR_REF_DATA_RESPONSE = SchemaPaths.REFERENCE_DATA
        + "/getMajorCredRefDataResponse.json";

    @MockitoSpyBean
    private JsonSchemaValidationService jsonSchemaValidationService;

    @Test
    @JiraEpic("PO-2630")
    @JiraStory("PO-2972")
    @DisplayName("Major creditors ref data when release 1b is disabled includes from suspense and excludes repayment")
    void getMajorCreditorsRefData_WhenRelease1bDisabled_IncludesFromSuspenseAndExcludesRepayment() throws Exception {
        ResultActions result = mockMvc.perform(get(MAJOR_CREDITORS_URL)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(AUTHORISATION_HEADER, userStateStub.getBearerToken()));

        String body = result.andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(145))
            .andExpect(jsonPath("$.refData[*].from_suspense").exists())
            .andExpect(jsonPath("$.refData[*].repayment").doesNotExist())
            .andReturn().getResponse().getContentAsString();

        jsonSchemaValidationService.validateOrError(body, GET_MAJOR_REF_DATA_RESPONSE);
    }
}
