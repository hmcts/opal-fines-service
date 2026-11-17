package uk.gov.hmcts.opal.controllers.r1c;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles("integration")
@TestPropertySource(properties = {
    "launchdarkly.default-flag-values.release-1c-payment=false"
})
@DisplayName("Mappings with Release 1C payment disabled")
class MappingsRelease1cPaymentDisabledIT extends AbstractIntegrationTest {

    @ParameterizedTest
    @ValueSource(strings = {"method-of-payment", "source", "suspense-reason"})
    @DisplayName("Payment mapping types return the standard feature-disabled problem")
    @JiraStory("PO-10964")
    @JiraEpic("PO-2439")
    void paymentMappingType_returnsFeatureDisabled(String type) throws Exception {
        mockMvc.perform(get("/mappings/{type}", type))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/feature-disabled"))
            .andExpect(jsonPath("$.title").value("Feature Disabled"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("The requested feature is not currently available"))
            .andExpect(jsonPath("$.retriable").value(false));
    }

    @Test
    @DisplayName("Defendant account status remains available with Release 1B enabled")
    @JiraStory("PO-10964")
    @JiraEpic("PO-2439")
    void defendantAccountStatus_remainsAvailable() throws Exception {
        mockMvc.perform(get("/mappings/defendant-account-status"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Unsupported type lists the available mappings when Release 1C Payment is disabled")
    @JiraStory("PO-10964")
    @JiraEpic("PO-2439")
    void unsupportedType_listsR1bSupportedTypes() throws Exception {
        mockMvc.perform(get("/mappings/unsupported-type"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/unsupported-mapping-type"))
            .andExpect(jsonPath("$.detail").value("Unsupported mapping type: unsupported-type. "
                + "Supported types: defendant-account-status, file-type"))
            .andExpect(jsonPath("$.mapping_type").value("unsupported-type"))
            .andExpect(jsonPath("$.supported_types", contains("defendant-account-status", "file-type")));
    }

    @Test
    @DisplayName("Missing type lists the available mappings when Release 1C Payment is disabled")
    @JiraStory("PO-10964")
    @JiraEpic("PO-2439")
    void missingType_listsR1bSupportedTypes() throws Exception {
        mockMvc.perform(get("/mappings"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/missing-mapping-type"))
            .andExpect(jsonPath("$.detail").value("Required mapping type is missing. "
                + "Supported types: defendant-account-status, file-type"))
            .andExpect(jsonPath("$.supported_types", contains("defendant-account-status", "file-type")));
    }
}
