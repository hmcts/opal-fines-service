package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationWithSecurityTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@TestPropertySource(properties = {
    "launchdarkly.default-flag-values.release-1c-payment=true"
})
@DisplayName("Outstanding Auto Payment Security Integration Tests")
@Sql(scripts = "classpath:db/insertData/insert_into_outstanding_auto_payment_count.sql",
     executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_outstanding_auto_payment_count.sql",
     executionPhase = AFTER_TEST_METHOD)
class OutstandingAutoPaymentSecurityIntegrationTest extends AbstractIntegrationWithSecurityTest {

    private static final String URL = "/business-units/outstanding-auto-payment-count";

    @Test
    @DisplayName("PO-2470 INT.08 - Returns outstanding counts using token permissions")
    @JiraStory("PO-2470")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-2470")
    void returnsCountsForPermittedTokenBusinessUnit() throws Exception {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions((short) 2470, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS);

        mockMvc.perform(get(URL)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.business_units", hasSize(1)))
            .andExpect(jsonPath("$.business_units[0].business_unit_id").value(2470))
            .andExpect(jsonPath("$.business_units[0].file_count").value(2))
            .andExpect(jsonPath("$.business_units[0].till_count").value(1));
    }

    @Test
    @DisplayName("PO-2470 INT.09 - Rejects request without token")
    @JiraStory("PO-2470")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-2470")
    void rejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get(URL)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/unauthorized"))
            .andExpect(jsonPath("$.title").value("Unauthorized"))
            .andExpect(jsonPath("$.detail").value("You are not authorized to access this resource"))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.properties.retriable").value(false));
    }
}
