package uk.gov.hmcts.opal.controllers;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.SEARCH_AND_VIEW_ACCOUNTS;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.generated.model.ReportInstanceListReportsInner;
import uk.gov.hmcts.opal.service.report.GenericReportService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@Sql(scripts = "classpath:db/insertData/insert_into_report_instances.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_report_instances.sql", executionPhase = AFTER_TEST_METHOD)
class ReportInstancesApiControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String REPORT_ID = "it_report_instances";
    private static final String URL_BASE = "/report-instances";

    @MockitoSpyBean
    private GenericReportService genericReportService;

    @BeforeEach
    void setUp() {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions((short) 10, SEARCH_AND_VIEW_ACCOUNTS);
        userStateStub.addPermissions((short) 20, SEARCH_AND_VIEW_ACCOUNTS);
    }

    private MockHttpServletRequestBuilder authorisedGet() {
        return get(URL_BASE)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header("authorization", userStateStub.getBearerToken());
    }

    private ReportInstanceListReportsInner readyDto() {
        ReportInstanceListReportsInner dto = new ReportInstanceListReportsInner();
        dto.setInstanceId(9001L);
        dto.setReportId(REPORT_ID);
        dto.setName("My Report");
        dto.setRequestedAt(java.time.LocalDateTime.of(2026, 1, 1, 10, 0));
        dto.setGeneratedAt(java.time.LocalDateTime.of(2026, 1, 1, 11, 0));
        dto.setNumberOfRecords(100);
        dto.setRequestedBy(new uk.gov.hmcts.opal.generated.model.UserByNameDetailsCommon()
            .userId("42")
            .name("John Doe"));
        dto.setBusinessUnits(List.of(
            new uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryCommon().businessUnitId("10"),
            new uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryCommon().businessUnitId("20")
        ));
        dto.setStatus(new uk.gov.hmcts.opal.generated.model.StatusReports()
            .code(uk.gov.hmcts.opal.generated.model.StatusReports.CodeEnum.READY)
            .displayName("Ready"));
        dto.setIsDownloadable(true);
        dto.setSupportedFileTypes(List.of(
            ReportInstanceListReportsInner.SupportedFileTypesEnum.CSV,
            ReportInstanceListReportsInner.SupportedFileTypesEnum.PDF
        ));
        return dto;
    }

    private ReportInstanceListReportsInner inProgressDto() {
        ReportInstanceListReportsInner dto = readyDto();
        dto.setInstanceId(9002L);
        dto.setName("Integration Report Instances");
        dto.setRequestedAt(java.time.LocalDateTime.of(2026, 2, 1, 10, 0));
        dto.setGeneratedAt(java.time.LocalDateTime.of(2026, 2, 1, 11, 0));
        dto.setRequestedBy(new uk.gov.hmcts.opal.generated.model.UserByNameDetailsCommon()
            .userId("43")
            .name("Jane Doe"));
        dto.setBusinessUnits(List.of(
            new uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryCommon().businessUnitId("10")
        ));
        dto.setStatus(new uk.gov.hmcts.opal.generated.model.StatusReports()
            .code(uk.gov.hmcts.opal.generated.model.StatusReports.CodeEnum.IN_PROGRESS)
            .displayName("In Progress"));
        dto.setIsDownloadable(false);
        dto.setNumberOfRecords(null);
        return dto;
    }

    @Nested
    class GetReportInstancesSadPath {

        @Test
        @JiraStory("PO-2251")
        @JiraEpic("PO-2248")
        @JiraTestKey("PO-8292")
        void whenNoTokenPresent_unauthorizedIsReturned_sadPath() throws Exception {
            mockMvc.perform(get(URL_BASE).param("report_id", REPORT_ID))
                .andExpectAll(
                    status().isUnauthorized(),
                    content().contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON),
                    jsonPath("$.title").value("Unauthorized"),
                    jsonPath("$.detail").value("Missing or invalid access token"),
                    jsonPath("$.status").value(401),
                    jsonPath("$.retriable").value(false)
                );
        }

        @Test
        @JiraStory("PO-2251")
        @JiraEpic("PO-2248")
        @JiraTestKey("PO-8293")
        void whenRequestNotAcceptable_notAcceptableIsReturned_sadPath() throws Exception {
            mockMvc.perform(get(URL_BASE)
                    .param("report_id", REPORT_ID)
                    .accept("application/xml"))
                .andExpect(status().isNotAcceptable());
        }

        @Test
        @JiraStory("PO-2251")
        @JiraEpic("PO-2248")
        @JiraTestKey("PO-8291")
        void whenUserLacksReportPermission_forbiddenIsReturned_sadPath() throws Exception {
            userStateStub.setupWithNoPermissions();
            doThrow(new org.springframework.security.access.AccessDeniedException(
                "User does not have permission for reportId: " + REPORT_ID
            )).when(genericReportService).searchReportInstances(null, null, null, null, REPORT_ID);

            mockMvc.perform(authorisedGet()
                    .param("report_id", REPORT_ID))
                .andExpectAll(
                    status().isForbidden(),
                    jsonPath("$.title").value("Forbidden"),
                    jsonPath("$.detail").value("You do not have permission to access this resource"),
                    jsonPath("$.status").value(403),
                    jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"),
                    jsonPath("$.retriable").value(false)
                );
        }

        @Test
        @JiraStory("PO-2251")
        @JiraEpic("PO-2248")
        @JiraTestKey("PO-8294")
        void whenUserLacksBusinessUnitPermission_forbiddenIsReturned_sadPath() throws Exception {
            doThrow(new org.springframework.security.access.AccessDeniedException(
                "User does not have permission for one or more specified business units"
            )).when(genericReportService).searchReportInstances(null, null, List.of(30), null, REPORT_ID);

            mockMvc.perform(authorisedGet()
                    .param("report_id", REPORT_ID)
                    .param("business_units", "30"))
                .andExpectAll(
                    status().isForbidden(),
                    jsonPath("$.title").value("Forbidden"),
                    jsonPath("$.detail").value("You do not have permission to access this resource"),
                    jsonPath("$.status").value(403),
                    jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"),
                    jsonPath("$.retriable").value(false)
                );
        }
    }

    @Nested
    class GetReportInstancesHappyPath {

        @Test
        @JiraStory("PO-2251")
        @JiraEpic("PO-2248")
        @JiraTestKey("PO-8286")
        void whenNoFilters_allInstancesAreReturned_happyPath() throws Exception {
            doReturn(List.of(readyDto(), inProgressDto(), readyDto()))
                .when(genericReportService).searchReportInstances(null, null, null, null, REPORT_ID);

            mockMvc.perform(authorisedGet()
                    .param("report_id", REPORT_ID))
                .andExpectAll(
                    status().isOk(),
                    jsonPath("$").isArray(),
                    jsonPath("$.length()").value(3),
                    jsonPath("$[0].report_id").value(REPORT_ID)
                );
        }

        @Test
        @JiraStory("PO-2251")
        @JiraEpic("PO-2248")
        @JiraTestKey("PO-8290")
        void whenFilteredByReportId_matchingInstancesAreReturned_happyPath() throws Exception {
            doReturn(List.of(readyDto(), inProgressDto(), readyDto()))
                .when(genericReportService).searchReportInstances(null, null, null, null, REPORT_ID);

            mockMvc.perform(authorisedGet()
                    .param("report_id", REPORT_ID))
                .andExpectAll(
                    status().isOk(),
                    jsonPath("$").isArray(),
                    jsonPath("$.length()").value(3),
                    jsonPath("$[0].report_id").value(REPORT_ID)
                );
        }

        @Test
        @JiraStory("PO-2251")
        @JiraEpic("PO-2248")
        @JiraTestKey("PO-8282")
        void whenFilteredByDateRange_matchingInstancesAreReturned_happyPath() throws Exception {
            doReturn(List.of(readyDto())).when(genericReportService).searchReportInstances(
                java.time.LocalDate.of(2026, 1, 1),
                java.time.LocalDate.of(2026, 1, 31),
                null,
                null,
                REPORT_ID
            );

            mockMvc.perform(authorisedGet()
                    .param("report_id", REPORT_ID)
                    .param("from_date", "2026-01-01")
                    .param("to_date", "2026-01-31"))
                .andExpectAll(
                    status().isOk(),
                    jsonPath("$").isArray(),
                    jsonPath("$.length()").value(1),
                    jsonPath("$[0].report_id").value(REPORT_ID)
                );
        }

        @Test
        @JiraStory("PO-2251")
        @JiraEpic("PO-2248")
        @JiraTestKey("PO-8289")
        void whenFilteredByUserId_matchingInstancesAreReturned_happyPath() throws Exception {
            doReturn(List.of(readyDto(), readyDto())).when(genericReportService).searchReportInstances(
                null,
                null,
                null,
                42,
                REPORT_ID
            );

            mockMvc.perform(authorisedGet()
                    .param("report_id", REPORT_ID)
                    .param("user_id", "42"))
                .andExpectAll(
                    status().isOk(),
                    jsonPath("$").isArray(),
                    jsonPath("$.length()").value(2),
                    jsonPath("$[0].report_id").value(REPORT_ID)
                );
        }

        @Test
        @JiraStory("PO-2251")
        @JiraEpic("PO-2248")
        @JiraTestKey("PO-8287")
        void whenFilteredByBusinessUnit_matchingInstancesAreReturned_happyPath() throws Exception {
            doReturn(List.of(readyDto(), readyDto())).when(genericReportService).searchReportInstances(
                null,
                null,
                List.of(20),
                null,
                REPORT_ID
            );

            mockMvc.perform(authorisedGet()
                    .param("report_id", REPORT_ID)
                    .param("business_units", "20"))
                .andExpectAll(
                    status().isOk(),
                    jsonPath("$").isArray(),
                    jsonPath("$.length()").value(2),
                    jsonPath("$[0].report_id").value(REPORT_ID)
                );
        }

        @Test
        @JiraStory("PO-2251")
        @JiraEpic("PO-2248")
        @JiraTestKey("PO-8288")
        void whenReadyInstanceReturned_allFieldsAreMapped_happyPath() throws Exception {
            doReturn(List.of(readyDto())).when(genericReportService).searchReportInstances(
                java.time.LocalDate.of(2026, 1, 1),
                java.time.LocalDate.of(2026, 1, 31),
                null,
                null,
                REPORT_ID
            );

            mockMvc.perform(authorisedGet()
                    .param("report_id", REPORT_ID)
                    .param("from_date", "2026-01-01")
                    .param("to_date", "2026-01-31"))
                .andExpectAll(
                    status().isOk(),
                    jsonPath("$[0].instance_id").value(9001),
                    jsonPath("$[0].report_id").value(REPORT_ID),
                    jsonPath("$[0].name").value("My Report"),
                    jsonPath("$[0].requested_at").value("2026-01-01T10:00:00"),
                    jsonPath("$[0].generated_at").value("2026-01-01T11:00:00"),
                    jsonPath("$[0].number_of_records").value(100),
                    jsonPath("$[0].requested_by.user_id").value("42"),
                    jsonPath("$[0].requested_by.name").value("John Doe"),
                    jsonPath("$[0].business_units[0].business_unit_id").value("10"),
                    jsonPath("$[0].status.code").value("READY"),
                    jsonPath("$[0].status.display_name").value("Ready"),
                    jsonPath("$[0].is_downloadable").value(true),
                    jsonPath("$[0].supported_file_types[0]").value("CSV"),
                    jsonPath("$[0].supported_file_types[1]").value("PDF")
                );
        }

        @Test
        @JiraStory("PO-2251")
        @JiraEpic("PO-2248")
        @JiraTestKey("PO-8283")
        void whenInProgressInstanceReturned_isNotDownloadable_happyPath() throws Exception {
            doReturn(List.of(inProgressDto())).when(genericReportService).searchReportInstances(
                java.time.LocalDate.of(2026, 2, 1),
                java.time.LocalDate.of(2026, 2, 28),
                null,
                null,
                REPORT_ID
            );

            mockMvc.perform(authorisedGet()
                    .param("report_id", REPORT_ID)
                    .param("from_date", "2026-02-01")
                    .param("to_date", "2026-02-28"))
                .andExpectAll(
                    status().isOk(),
                    jsonPath("$[0].report_id").value(REPORT_ID),
                    jsonPath("$[0].status.code").value("IN_PROGRESS"),
                    jsonPath("$[0].status.display_name").value("In Progress"),
                    jsonPath("$[0].is_downloadable").value(false)
                );
        }

        @Test
        @JiraStory("PO-2251")
        @JiraEpic("PO-2248")
        @JiraTestKey("PO-8285")
        void whenInstanceNameNull_reportTitleIsUsed_happyPath() throws Exception {
            doReturn(List.of(inProgressDto())).when(genericReportService).searchReportInstances(
                java.time.LocalDate.of(2026, 2, 1),
                java.time.LocalDate.of(2026, 2, 28),
                null,
                null,
                REPORT_ID
            );

            mockMvc.perform(authorisedGet()
                    .param("report_id", REPORT_ID)
                    .param("from_date", "2026-02-01")
                    .param("to_date", "2026-02-28"))
                .andExpectAll(
                    status().isOk(),
                    jsonPath("$[0].report_id").value(REPORT_ID),
                    jsonPath("$[0].name").value("Integration Report Instances")
                );
        }

        @Test
        @JiraStory("PO-2251")
        @JiraEpic("PO-2248")
        @JiraTestKey("PO-8284")
        void whenNoMatchingInstances_emptyArrayReturned_happyPath() throws Exception {
            doReturn(List.of()).when(genericReportService).searchReportInstances(
                java.time.LocalDate.of(2020, 2, 1),
                java.time.LocalDate.of(2020, 2, 28),
                null,
                null,
                REPORT_ID
            );

            mockMvc.perform(authorisedGet()
                    .param("report_id", REPORT_ID)
                    .param("from_date", "2020-02-01")
                    .param("to_date", "2020-02-28"))
                .andExpectAll(
                    status().isOk(),
                    jsonPath("$").isArray(),
                    jsonPath("$.length()").value(0)
                );
        }
    }
}
