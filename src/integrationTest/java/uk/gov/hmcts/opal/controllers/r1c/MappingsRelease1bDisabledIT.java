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
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=false",
    "launchdarkly.default-flag-values.release-1c-payment=true"
})
@DisplayName("Mappings with Release 1B disabled")
class MappingsRelease1bDisabledIT extends AbstractIntegrationTest {

    @ParameterizedTest
    @ValueSource(strings = {"method-of-payment", "source", "suspense-reason"})
    @DisplayName("Payment mapping types remain available with Release 1C payment enabled")
    @JiraStory("PO-10964")
    @JiraEpic("PO-2439")
    void paymentMappingTypes_remainAvailable(String type) throws Exception {
        mockMvc.perform(get("/mappings/{type}", type))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @ParameterizedTest
    @ValueSource(strings = {"defendant-account-status", "file-type"})
    @DisplayName("Existing mapping types remain Release 1B guarded")
    @JiraStory("PO-10964")
    @JiraEpic("PO-2439")
    void otherMappingTypes_remainRelease1bProtected(String type) throws Exception {
        mockMvc.perform(get("/mappings/{type}", type))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/feature-disabled"));
    }

    @Test
    @DisplayName("Unsupported type lists the available payment mappings when Release 1B is disabled")
    @JiraStory("PO-10964")
    @JiraEpic("PO-2439")
    void unsupportedType_listsR1cPaymentMappings() throws Exception {
        mockMvc.perform(get("/mappings/unsupported-type"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/unsupported-mapping-type"))
            .andExpect(jsonPath("$.detail").value("Unsupported mapping type: unsupported-type. "
                + "Supported types: method-of-payment, source, suspense-reason"))
            .andExpect(jsonPath("$.mapping_type").value("unsupported-type"))
            .andExpect(jsonPath("$.supported_types", contains("method-of-payment", "source", "suspense-reason")));
    }

    @Test
    @DisplayName("Missing type lists the available payment mappings when Release 1B is disabled")
    @JiraStory("PO-10964")
    @JiraEpic("PO-2439")
    void missingType_listsR1cPaymentMappings() throws Exception {
        mockMvc.perform(get("/mappings"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/missing-mapping-type"))
            .andExpect(jsonPath("$.detail").value("Required mapping type is missing. "
                + "Supported types: method-of-payment, source, suspense-reason"))
            .andExpect(jsonPath("$.supported_types", contains("method-of-payment", "source", "suspense-reason")));
    }
}
