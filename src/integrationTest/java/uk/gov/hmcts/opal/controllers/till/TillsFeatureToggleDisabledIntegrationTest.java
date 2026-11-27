package uk.gov.hmcts.opal.controllers.till;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles("integration")
@TestPropertySource(properties = "launchdarkly.default-flag-values.release-1c-payment=false")
@DisplayName("Tills Feature Toggle Integration Tests")
class TillsFeatureToggleDisabledIntegrationTest extends AbstractIntegrationTest {

    @Test
    @JiraStory("PO-11365")
    @JiraEpic("PO-2439")
    void rejectsCreationWhenDisabled() throws Exception {
        mockMvc.perform(post("/tills").with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .contentType(MediaType.APPLICATION_JSON).content("""
                    {"business_unit_id":78,"payments_in":[{"defendant_account_id":123,
                     "payment_details":{"amount":12.34,"method":"NC","destination_type":"F",
                      "allocation_type":"FULL"},"additional_information":{"payment_received_from":"D"}}]}
                    """))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Feature Disabled"));
    }

    @Test
    @JiraStory("PO-3423")
    @JiraEpic("PO-2116")
    void rejectsAllocationWhenDisabled() throws Exception {
        mockMvc.perform(post("/tills/allocate").with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tills\":[{\"till_id\":1,\"business_unit_id\":78}]}"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Feature Disabled"));
    }

    @Test
    @DisplayName("PO-8362 INT.04 - Returns not found when Release 1C Payment is disabled")
    @JiraStory("PO-8362")
    @JiraStory("PO-3629")
    @JiraEpic("PO-3635")
    void rejectsDisabledFeature() throws Exception {
        mockMvc.perform(get("/tills/99000000836200"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Feature Disabled"));
    }
}
