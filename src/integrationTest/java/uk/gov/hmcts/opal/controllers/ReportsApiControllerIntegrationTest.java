package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Slf4j(topic = "opal.ReportsApiControllerIntegrationTest")
@Sql(scripts = "classpath:db/insertData/insert_into_reports.sql", executionPhase = BEFORE_TEST_CLASS)
@DisplayName("ReportsApiController Integration Test")
class ReportsApiControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/reports";

    // JSONPath constants
    private static final String JSON_REPORT_ID = "$.report_id";
    private static final String JSON_REPORT_TITLE = "$.report_title";
    private static final String JSON_REPORT_GROUP = "$.report_group";
    private static final String JSON_SUPPORTED_FILE_TYPES = "$.supported_file_types";
    private static final String JSON_AUDITED_REPORT = "$.audited_report";
    private static final String JSON_REPORT_PARAMETERS = "$.report_parameters";
    private static final String JSON_SUPPORTS_MULTI_BU = "$.supports_multiple_business_units";
    private static final String JSON_IS_BESPOKE_JOURNEY = "$.is_bespoke_journey";
    private static final String JSON_SHOWN_AS_WORKLIST = "$.shown_as_worklist";
    private static final String JSON_RETENTION_PERIOD = "$.retention_period";
    private static final String JSON_PERMISSION = "$.permission";
    private static final String JSON_CAN_MANUALLY_CREATE = "$.can_manually_create";

    @Nested
    @DisplayName("GET /reports/{id} - Success Cases")
    class GetReportByIdSuccessCases {

        @Test
        @DisplayName("Get report by ID - operational_report_enforcement [@PO-2250]")
        void getReportByIdOperationalEnforcement() throws Exception {
            ResultActions actions = mockMvc.perform(get(URL_BASE + "/operational_report_enforcement"));

            logResponseBody(actions, "getReportByIdOperationalEnforcement");

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(JSON_REPORT_ID).value("operational_report_enforcement"))
                .andExpect(jsonPath(JSON_REPORT_TITLE).value("Operational report (by enforcement)"))
                .andExpectAll(commonOperationalReportAssertions())
                .andExpect(jsonPath(JSON_RETENTION_PERIOD).value("P14D"))
                .andExpect(jsonPath(JSON_PERMISSION).value(nullValue()))
                .andExpect(jsonPath(JSON_CAN_MANUALLY_CREATE).value(true));
        }

        @Test
        @DisplayName("Get report by ID - operational_report_payment [@PO-2250]")
        void getReportByIdOperationalPayment() throws Exception {
            ResultActions actions = mockMvc.perform(get(URL_BASE + "/operational_report_payment"));

            logResponseBody(actions, "getReportByIdOperationalPayment");

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(JSON_REPORT_ID).value("operational_report_payment"))
                .andExpect(jsonPath(JSON_REPORT_TITLE).value("Operational report (by payment)"))
                .andExpectAll(commonOperationalReportAssertions())
                .andExpect(jsonPath(JSON_RETENTION_PERIOD).value("P14D"))
                .andExpect(jsonPath(JSON_CAN_MANUALLY_CREATE).value(true));
        }

        @Test
        @DisplayName("Get report validates correct data types for all fields [@PO-2250]")
        void getReportByIdValidatesDataTypes() throws Exception {
            ResultActions actions = mockMvc.perform(get(URL_BASE + "/operational_report_enforcement"));

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Validate string fields
                .andExpect(jsonPath(JSON_REPORT_ID).isString())
                .andExpect(jsonPath(JSON_REPORT_TITLE).isString())
                .andExpect(jsonPath(JSON_REPORT_GROUP).isString())
                // Validate array field
                .andExpect(jsonPath(JSON_SUPPORTED_FILE_TYPES).isArray())
                // Validate boolean fields
                .andExpect(jsonPath(JSON_AUDITED_REPORT).isBoolean())
                .andExpect(jsonPath(JSON_SUPPORTS_MULTI_BU).isBoolean())
                .andExpect(jsonPath(JSON_IS_BESPOKE_JOURNEY).isBoolean())
                .andExpect(jsonPath(JSON_SHOWN_AS_WORKLIST).isBoolean())
                .andExpect(jsonPath(JSON_CAN_MANUALLY_CREATE).isBoolean())
                // Validate string retention period
                .andExpect(jsonPath(JSON_RETENTION_PERIOD).isString());
        }

        // Helper methods

        private void logResponseBody(ResultActions actions, String testName) throws Exception {
            String body = actions.andReturn().getResponse().getContentAsString();
            log.info(":{}}: Response body:\n{}", testName, ToJsonString.toPrettyJson(body));
        }

        private ResultMatcher[] commonOperationalReportAssertions() {
            return new ResultMatcher[] {
                jsonPath(JSON_REPORT_GROUP).value("Operational Reports"),
                jsonPath(JSON_SUPPORTED_FILE_TYPES, hasSize(2)),
                jsonPath(JSON_AUDITED_REPORT).value(false),
                jsonPath(JSON_REPORT_PARAMETERS).value(nullValue()),
                jsonPath(JSON_SUPPORTS_MULTI_BU).value(false),
                jsonPath(JSON_IS_BESPOKE_JOURNEY).value(false),
                jsonPath(JSON_SHOWN_AS_WORKLIST).value(false)
            };
        }
    }

    @Nested
    @DisplayName("GET /reports/{id} - Error Cases")
    class GetReportByIdErrorCases {

        @Test
        @DisplayName("No report returned when report does not exist [@PO-2250]")
        void getReportByIdWhenReportDoesNotExist() throws Exception {
            mockMvc.perform(get(URL_BASE + "/non_existent_report"))
                .andExpect(status().isNotFound());
        }
    }
}

