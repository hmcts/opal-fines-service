package uk.gov.hmcts.opal.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.SEARCH_AND_VIEW_ACCOUNTS;
import static uk.gov.hmcts.opal.entity.report.SupportedFileType.CSV;
import static uk.gov.hmcts.opal.entity.report.SupportedFileType.PDF;

import jakarta.servlet.ServletException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.common.exceptions.standard.UnauthorizedException;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.repository.ReportRepository;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@Sql(scripts = "classpath:db/insertData/insert_into_report_instances.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_report_instances.sql", executionPhase = AFTER_TEST_METHOD)
class ReportInstancesApiControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String REPORT_ID = "it_report_instances";
    private static final String URL_BASE = "/report-instances";

    @MockitoBean
    private ReportRepository reportRepository;

    @BeforeEach
    void setUp() {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions((short) 10, SEARCH_AND_VIEW_ACCOUNTS);
        userStateStub.addPermissions((short) 20, SEARCH_AND_VIEW_ACCOUNTS);

        ReportEntity report = ReportEntity.builder()
            .reportId(REPORT_ID)
            .reportTitle("Integration Report Instances")
            .permission(SEARCH_AND_VIEW_ACCOUNTS)
            .supportedFileTypes(List.of(CSV, PDF))
            .build();

        when(reportRepository.existsById(anyString())).thenReturn(false);
        when(reportRepository.existsById(REPORT_ID)).thenReturn(true);
        when(reportRepository.findAll()).thenReturn(List.of(report));
    }

    private MockHttpServletRequestBuilder authorisedGet() {
        return get(URL_BASE)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header("authorization", userStateStub.getBearerToken());
    }

    @Nested
    class GetReportInstancesSadPath {

        @Test
        @JiraStory("PO-2251")
        @JiraEpic("PO-2248")
        void whenNoTokenPresent_unauthorizedIsReturned_sadPath() throws Exception {
            ServletException exception = assertThrows(
                ServletException.class,
                () -> mockMvc.perform(get(URL_BASE).param("report_id", REPORT_ID))
            );

            assertThat(exception.getCause())
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Current user is not authenticated with OpalJwtAuthenticationToken");
        }

        @Test
        @JiraStory("PO-2251")
        @JiraEpic("PO-2248")
        void whenRequestNotAcceptable_notAcceptableIsReturned_sadPath() throws Exception {
            mockMvc.perform(get(URL_BASE)
                    .param("report_id", REPORT_ID)
                    .accept("application/xml"))
                .andExpect(status().isNotAcceptable());
        }

        @Test
        @JiraStory("PO-2251")
        @JiraEpic("PO-2248")
        void whenUserLacksReportPermission_forbiddenIsReturned_sadPath() throws Exception {
            userStateStub.setupWithNoPermissions();

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
        void whenUserLacksBusinessUnitPermission_forbiddenIsReturned_sadPath() throws Exception {
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
        void whenNoFilters_allInstancesAreReturned_happyPath() throws Exception {
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
        void whenFilteredByReportId_matchingInstancesAreReturned_happyPath() throws Exception {
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
        void whenFilteredByDateRange_matchingInstancesAreReturned_happyPath() throws Exception {
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
        void whenFilteredByUserId_matchingInstancesAreReturned_happyPath() throws Exception {
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
        void whenFilteredByBusinessUnit_matchingInstancesAreReturned_happyPath() throws Exception {
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
        void whenReadyInstanceReturned_allFieldsAreMapped_happyPath() throws Exception {
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
        void whenInProgressInstanceReturned_isNotDownloadable_happyPath() throws Exception {
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
        void whenInstanceNameNull_reportTitleIsUsed_happyPath() throws Exception {
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
        void whenNoMatchingInstances_emptyArrayReturned_happyPath() throws Exception {
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
