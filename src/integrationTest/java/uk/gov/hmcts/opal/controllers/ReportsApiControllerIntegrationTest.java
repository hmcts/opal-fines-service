package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

class ReportsApiControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/reports";


    @BeforeEach
    void setUp() {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions((short) 70, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
    }

    @Test
    @DisplayName("Get report by ID - report does not exist [@PO-2250]")
    @JiraStory("PO-2250")
    @JiraEpic("PO-2248")
    void getReportById_whenReportDoesNotExist_returns404() throws Exception {

        mockMvc.perform(get(URL_BASE + "/non_existent_report")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor()))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get report by ID - no token present returns 403 [@PO-2250]")
    @JiraStory("PO-2250")
    @JiraEpic("PO-2248")
    void getReportById_whenNoTokenPresent_returns403() throws Exception {
        userStateStub.setupWithNoPermissions();
        mockMvc.perform(get(URL_BASE + "/operational_report_enforcement"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Get report by ID - not acceptable for invalid format request [@PO-2250]")
    @JiraStory("PO-2250")
    @JiraEpic("PO-2248")
    void getReportById_whenRequestNotAcceptable_returns406() throws Exception {
        mockMvc.perform(get(URL_BASE + "/operational_report_enforcement")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .accept("application/xml"))
            .andExpect(status().isNotAcceptable());
    }

    @Nested
    @Sql(scripts = "classpath:db/insertData/insert_into_reports.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/deleteData/delete_from_reports.sql", executionPhase = AFTER_TEST_METHOD)
    class GetReportByIdSuccessCases {

        @Test
        @DisplayName("Get report by ID - all fields populated [@PO-2250]")
        @JiraStory("PO-2250")
        @JiraEpic("PO-2248")
        void getReportById_whenAllFieldsPopulated_returns200AndMapsAllFields() throws Exception {
            mockMvc.perform(get(URL_BASE + "/it_report_full")
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.report_id").value("it_report_full"))
                .andExpect(jsonPath("$.report_title").value("Integration Full Report"))
                .andExpect(jsonPath("$.report_group").value("Operational Reports"))
                .andExpect(jsonPath("$.supported_file_types[0]").value("CSV"))
                .andExpect(jsonPath("$.supported_file_types[1]").value("PDF"))
                .andExpect(jsonPath("$.supported_file_types[2]").value("XML"))
                .andExpect(jsonPath("$.audited_report").value(true))
                .andExpect(jsonPath("$.report_parameters").isMap())
                .andExpect(jsonPath("$.supports_multiple_business_units").value(true))
                .andExpect(jsonPath("$.is_bespoke_journey").value(true))
                .andExpect(jsonPath("$.shown_as_worklist").value(true))
                .andExpect(jsonPath("$.retention_period").value("PT720H"))
                .andExpect(jsonPath("$.permission").value(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS.name()))
                .andExpect(jsonPath("$.can_manually_create").value(false));
        }

        @Test
        @DisplayName("Get report by ID - optional fields null or empty [@PO-2250]")
        @JiraStory("PO-2250")
        @JiraEpic("PO-2248")
        void getReportById_whenOptionalFieldsNullOrEmpty_returns200() throws Exception {
            mockMvc.perform(get(URL_BASE + "/it_report_optional")
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.report_id").value("it_report_optional"))
                .andExpect(jsonPath("$.report_parameters").isMap())
                .andExpect(jsonPath("$.report_parameters").isEmpty())
                .andExpect(jsonPath("$.supported_file_types").isArray())
                .andExpect(jsonPath("$.supported_file_types").isEmpty())
                .andExpect(jsonPath("$.permission").value(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS.name()));
        }

        @Test
        @DisplayName("Get report by ID - retention period is ISO-8601 [@PO-2250]")
        @JiraStory("PO-2250")
        @JiraEpic("PO-2248")
        void getReportById_whenRetentionPeriodPresent_returnsIso8601String() throws Exception {
            mockMvc.perform(get(URL_BASE + "/it_report_full")
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.retention_period").value("PT720H"));
        }

        @Test
        @DisplayName("Get report by ID - report parameters as JSON object [@PO-2250]")
        @JiraStory("PO-2250")
        @JiraEpic("PO-2248")
        void getReportById_whenReportParametersPresent_returnsJsonObject() throws Exception {
            mockMvc.perform(get(URL_BASE + "/it_report_full")
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.report_parameters").isMap())
                .andExpect(jsonPath("$.report_parameters.filters.status[0]").value("ACTIVE"))
                .andExpect(jsonPath("$.report_parameters.filters.status[1]").value("PENDING"))
                .andExpect(jsonPath("$.report_parameters.options.includeArchived").value(false));
        }

        @Test
        @DisplayName("Get report by ID - supported file types preserve order and enums [@PO-2250]")
        @JiraStory("PO-2250")
        @JiraEpic("PO-2248")
        void getReportById_whenSupportedFileTypesPresent_preservesOrderAndEnums() throws Exception {
            mockMvc.perform(get(URL_BASE + "/it_report_order")
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.supported_file_types[0]").value("XML"))
                .andExpect(jsonPath("$.supported_file_types[1]").value("CSV"))
                .andExpect(jsonPath("$.supported_file_types[2]").value("PDF"))
                .andExpect(jsonPath("$.supported_file_types.length()").value(3));
        }

        @Test
        @DisplayName("Get report by ID - only documented fields returned [@PO-2250]")
        @JiraStory("PO-2250")
        @JiraEpic("PO-2248")
        void getReportById_whenSuccessful_returnsOnlyDocumentedFields() throws Exception {
            ResultActions actions = mockMvc.perform(get(URL_BASE + "/it_report_full")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor()));

            String body = actions.andReturn().getResponse().getContentAsString();
            Set<String> actualFields = objectMapper.readTree(body).properties()
                .stream()
                .map(entry -> entry.getKey())
                .collect(java.util.stream.Collectors.toSet());

            actions.andExpect(status().isOk());

            org.junit.jupiter.api.Assertions.assertEquals(Set.of(
                "report_id",
                "report_title",
                "report_group",
                "supported_file_types",
                "audited_report",
                "report_parameters",
                "supports_multiple_business_units",
                "is_bespoke_journey",
                "shown_as_worklist",
                "retention_period",
                "permission",
                "can_manually_create"
            ), actualFields);
        }

        @Test
        @DisplayName("Get report by ID - response is idempotent [@PO-2250]")
        @JiraStory("PO-2250")
        @JiraEpic("PO-2248")
        void getReportById_whenCalledRepeatedly_returnsIdenticalOutput() throws Exception {
            ResultActions first = mockMvc.perform(get(URL_BASE + "/it_report_full")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor()));
            ResultActions second = mockMvc.perform(get(URL_BASE + "/it_report_full")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor()));

            String firstBody = first.andReturn().getResponse().getContentAsString();
            String secondBody = second.andReturn().getResponse().getContentAsString();

            first.andExpect(status().isOk());
            second.andExpect(status().isOk());

            org.junit.jupiter.api.Assertions.assertEquals(
                objectMapper.readTree(firstBody),
                objectMapper.readTree(secondBody),
                ToJsonString.toPrettyJson(secondBody)
            );
        }
    }

    @Nested
    class GetReportByIdPermissionCases {

        static Stream<Arguments> reportCases() {
            return Stream.of(
                Arguments.of("operational_report_enforcement"),
                Arguments.of("operational_report_payment")
            );
        }

        @ParameterizedTest
        @MethodSource("reportCases")
        @DisplayName("Get report by ID - null permission returns forbidden [@PO-2250]")
        @JiraStory("PO-2250")
        @JiraEpic("PO-2248")
        void getReportById_whenReportPermissionIsNull_returns403(String reportId) throws Exception {
            mockMvc.perform(get(URL_BASE + "/" + reportId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Forbidden"))
                .andExpect(jsonPath("$.detail").value("You do not have permission to access this resource"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"))
                .andExpect(jsonPath("$.retriable").value(false));
        }

        @Test
        @DisplayName("Get report by ID - user lacks permission [@PO-2250]")
        @JiraStory("PO-2250")
        @JiraEpic("PO-2248")
        void getReportById_whenUserLacksPermission_returns403() throws Exception {
            userStateStub.setupWithNoPermissions();
            mockMvc.perform(get(URL_BASE + "/operational_report_enforcement"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Forbidden"))
                .andExpect(jsonPath("$.detail").value("You do not have permission to access this resource"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"))
                .andExpect(jsonPath("$.retriable").value(false));
        }

        @Test
        @DisplayName("Get report by ID - user lacks required report permission [@PO-2250]")
        @Sql(scripts = "classpath:db/insertData/insert_into_reports.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:db/deleteData/delete_from_reports.sql", executionPhase = AFTER_TEST_METHOD)
        @JiraStory("PO-2250")
        @JiraEpic("PO-2248")
        void getReportById_whenUserLacksRequiredReportPermission_returns403() throws Exception {
            userStateStub.setupWithNoPermissions();

            mockMvc.perform(get(URL_BASE + "/it_report_full")
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Forbidden"))
                .andExpect(jsonPath("$.detail").value("You do not have permission to access this resource"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"))
                .andExpect(jsonPath("$.retriable").value(false));
        }
    }

    @Nested
    @Sql(
        statements =
            "UPDATE reports SET permission = 'SEARCH_AND_VIEW_ACCOUNTS' "
                + "WHERE report_id IN ('operational_report_enforcement', 'operational_report_payment')",
        executionPhase = BEFORE_TEST_METHOD
    )
    @Sql(
        statements =
            "UPDATE reports SET permission = NULL "
                + "WHERE report_id IN ('operational_report_enforcement', 'operational_report_payment')",
        executionPhase = AFTER_TEST_METHOD
    )
    class GetOperationalReportThresholdCases {

        @ParameterizedTest
        @MethodSource("uk.gov.hmcts.opal.controllers.ReportsApiControllerIntegrationTest#operationalReportCases")
        @DisplayName("Get report by ID - operational reports include BU warning threshold [@PO-7225]")
        @JiraStory("PO-7225")
        @JiraEpic("PO-2248")
        void getReportById_whenOperationalReport_returnsBuWarningThreshold(String reportId) throws Exception {
            mockMvc.perform(get(URL_BASE + "/" + reportId)
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.report_parameters").isMap())
                .andExpect(jsonPath("$.report_parameters.business_unit_warning_threshold").value(10));
        }

        @Test
        @DisplayName("Get report by ID - non operational report does not include BU warning threshold [@PO-7225]")
        @Sql(scripts = "classpath:db/insertData/insert_into_reports.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:db/deleteData/delete_from_reports.sql", executionPhase = AFTER_TEST_METHOD)
        @JiraStory("PO-7225")
        @JiraEpic("PO-2248")
        void getReportById_whenNonOperationalReport_returnsUnchangedParameters() throws Exception {
            mockMvc.perform(get(URL_BASE + "/it_report_optional")
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.report_parameters").isMap())
                .andExpect(jsonPath("$.report_parameters").isEmpty())
                .andExpect(jsonPath("$.report_parameters.business_unit_warning_threshold").doesNotExist());
        }

        @Test
        @DisplayName("Get report by ID - missing BU warning threshold config returns 500 [@PO-7225]")
        @Sql(
            statements = {
                "UPDATE reports SET permission = 'SEARCH_AND_VIEW_ACCOUNTS' "
                    + "WHERE report_id = 'operational_report_enforcement'",
                "DELETE FROM configuration_items WHERE item_name = 'OPERATIONAL_REPORT_BU_WARNING_THRESHOLD'"
            },
            executionPhase = BEFORE_TEST_METHOD
        )
        @Sql(
            statements = {
                "INSERT INTO configuration_items "
                    + "(configuration_item_id, item_name, business_unit_id, item_value, item_values) "
                    + "VALUES (60000000000014, 'OPERATIONAL_REPORT_BU_WARNING_THRESHOLD', NULL, '10', NULL)",
                "UPDATE reports SET permission = NULL WHERE report_id = 'operational_report_enforcement'"
            },
            executionPhase = AFTER_TEST_METHOD
        )
        @JiraStory("PO-7225")
        @JiraEpic("PO-2248")
        void getReportById_whenThresholdConfigMissing_returns500() throws Exception {
            mockMvc.perform(get(URL_BASE + "/operational_report_enforcement")
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.detail")
                    .value("Missing configuration item: OPERATIONAL_REPORT_BU_WARNING_THRESHOLD"))
                .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/internal-server-error"))
                .andExpect(jsonPath("$.retriable").value(false));
        }

        @ParameterizedTest(
            name = "Get report by ID - invalid BU warning threshold ''{0}'' returns 500 [@PO-7225]"
        )
        @MethodSource("uk.gov.hmcts.opal.controllers.ReportsApiControllerIntegrationTest#invalidThresholdValues")
        @JiraStory("PO-7225")
        @JiraEpic("PO-2248")
        void getReportById_whenThresholdConfigInvalid_returns500(String invalidThresholdValue) throws Exception {
            jdbcTemplate.update(
                "UPDATE reports SET permission = 'SEARCH_AND_VIEW_ACCOUNTS' "
                    + "WHERE report_id = 'operational_report_enforcement'"
            );
            try {
                jdbcTemplate.update(
                    "UPDATE configuration_items SET item_value = ? "
                        + "WHERE item_name = 'OPERATIONAL_REPORT_BU_WARNING_THRESHOLD' AND business_unit_id IS NULL",
                    invalidThresholdValue
                );

                mockMvc.perform(get(URL_BASE + "/operational_report_enforcement")
                        .with(userStateStub.getAuthenticaitonRequestPostProcessor()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.title").value("Internal Server Error"))
                    .andExpect(jsonPath("$.detail")
                        .value("Invalid positive integer configuration item: OPERATIONAL_REPORT_BU_WARNING_THRESHOLD"))
                    .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/internal-server-error"))
                    .andExpect(jsonPath("$.retriable").value(false));
            } finally {
                jdbcTemplate.update(
                    "UPDATE configuration_items SET item_value = '10' "
                        + "WHERE item_name = 'OPERATIONAL_REPORT_BU_WARNING_THRESHOLD' AND business_unit_id IS NULL"
                );
                jdbcTemplate.update(
                    "UPDATE reports SET permission = NULL WHERE report_id = 'operational_report_enforcement'"
                );
            }
        }
    }

    static Stream<Arguments> operationalReportCases() {
        return Stream.of(
            Arguments.of("operational_report_enforcement"),
            Arguments.of("operational_report_payment")
        );
    }

    static Stream<Arguments> invalidThresholdValues() {
        return Stream.of(
            Arguments.of("not-an-integer"),
            Arguments.of("0"),
            Arguments.of("-1")
        );
    }
}
