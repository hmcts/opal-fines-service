package uk.gov.hmcts.opal.controllers.r1c;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration"})
@DisplayName("MappingsController Integration Test")
class MappingsControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("GET /mappings/{type} returns defendant account status mappings")
    @JiraStory("PO-3871")
    @JiraEpic("PO-3372")
    @JiraTestKey("PO-9439")
    void getMappings_returnsSupportedDefendantAccountStatusMappings() throws Exception {
        mockMvc.perform(get("/mappings/defendant-account-status"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(6)))
            .andExpect(jsonPath("$[*].code", contains("CS", "L", "TA", "TO", "TS", "WO")))
            .andExpect(jsonPath("$[*].display_name", contains(
                "Account consolidated",
                "Live",
                "TFO acknowledged",
                "TFO to be acknowledged",
                "TFO to NI/Scotland to be acknowledged",
                "Account written off"
            )))
            .andExpect(jsonPath("$[0].label").doesNotExist());
    }

    @Test
    @DisplayName("GET /mappings/{type} returns file type mappings")
    @JiraStory("PO-10576")
    @JiraEpic("PO-3516")
    void getMappings_returnsSupportedFileTypeMappings() throws Exception {
        mockMvc.perform(get("/mappings/file-type"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(4)))

            .andExpect(jsonPath("$[0].code").value("CSV"))
            .andExpect(jsonPath("$[0].display_name").value("CSV"))
            .andExpect(jsonPath("$[0].mime_type").value("text/csv"))

            .andExpect(jsonPath("$[1].code").value("PDF"))
            .andExpect(jsonPath("$[1].display_name").value("PDF"))
            .andExpect(jsonPath("$[1].mime_type").value("application/pdf"))

            .andExpect(jsonPath("$[2].code").value("JSON"))
            .andExpect(jsonPath("$[2].display_name").value("JSON"))
            .andExpect(jsonPath("$[2].mime_type").value("application/json"))

            .andExpect(jsonPath("$[3].code").value("XML"))
            .andExpect(jsonPath("$[3].display_name").value("XML"))
            .andExpect(jsonPath("$[3].mime_type").value("application/xml"));
    }

    @Test
    @DisplayName("GET /mappings/{type} returns 400 with supported types for unsupported mapping type")
    @JiraStory("PO-3871")
    @JiraEpic("PO-3372")
    @JiraTestKey("PO-9438")
    void getMappings_whenTypeIsUnsupported_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/mappings/unsupported-type"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Unsupported Mapping Type"))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/unsupported-mapping-type"))
            .andExpect(jsonPath("$.detail").value(
                "Unsupported mapping type: unsupported-type. Supported types: defendant-account-status, file-type,"
                    + " method-of-payment, source, suspense-reason"
            ))
            .andExpect(jsonPath("$.mapping_type").value("unsupported-type"))
            .andExpect(jsonPath("$.supported_types", hasSize(5)))
            .andExpect(jsonPath("$.supported_types", contains("defendant-account-status", "file-type",
                "method-of-payment", "source", "suspense-reason")));
    }

    @Test
    @DisplayName("GET /mappings returns 400 when mapping type is missing")
    @JiraStory("PO-3871")
    @JiraEpic("PO-3372")
    @JiraTestKey("PO-9437")
    void getMappings_whenTypeIsMissing_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/mappings"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Missing Mapping Type"))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/missing-mapping-type"))
            .andExpect(jsonPath("$.detail").value(
                "Required mapping type is missing. Supported types: defendant-account-status, file-type,"
                    + " method-of-payment, source, suspense-reason"
            ))
            .andExpect(jsonPath("$.supported_types", hasSize(5)))
            .andExpect(jsonPath("$.supported_types", contains("defendant-account-status", "file-type",
                "method-of-payment", "source", "suspense-reason")));
    }

    @Test
    @DisplayName("GET /mappings/method-of-payment returns the payment methods")
    @JiraStory("PO-10964")
    @JiraEpic("PO-2439")
    void getMappings_returnsMethodOfPaymentMappings() throws Exception {
        mockMvc.perform(get("/mappings/method-of-payment"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(4)))
            .andExpect(jsonPath("$[*].code", contains("NC", "PO", "CQ", "CT")))
            .andExpect(jsonPath("$[*].display_name", contains(
                "Notes and coins", "Postal order", "Cheque", "Credit transfer")));
    }

    @Test
    @DisplayName("GET /mappings/source returns the payment sources")
    @JiraStory("PO-10964")
    @JiraEpic("PO-2439")
    void getMappings_returnsSourceMappings() throws Exception {
        mockMvc.perform(get("/mappings/source"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(6)))
            .andExpect(jsonPath("$[*].code", contains(
                "ALLPAY", "BARCLAYCARD", "BTECKOH", "DWP", "NATWEST", "OTHER")))
            .andExpect(jsonPath("$[*].display_name", contains(
                "allpay", "Barclaycard", "British Telecom", "DWP/AEA", "NatWest", "Other")));
    }

    @Test
    @DisplayName("GET /mappings/suspense-reason returns the suspense reasons")
    @JiraStory("PO-10964")
    @JiraEpic("PO-2439")
    void getMappings_returnsSuspenseReasonMappings() throws Exception {
        mockMvc.perform(get("/mappings/suspense-reason"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(4)))
            .andExpect(jsonPath("$[*].code", contains("OF", "FA", "MS", "UN")))
            .andExpect(jsonPath("$[*].display_name", contains(
                "Fines overpayment", "Fine paid in advance", "Miscellaneous payment", "Unidentified Amount")));
    }
}
