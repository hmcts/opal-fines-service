package uk.gov.hmcts.opal.controllers;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.SEARCH_AND_VIEW_ACCOUNTS;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@Sql(scripts = "classpath:db/insertData/insert_into_report_instances.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_report_instances.sql", executionPhase = AFTER_TEST_METHOD)
class ReportInstancesApiControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String REPORT_ID = "it_report_instances";
    private static final String URL_BASE = "/report-instances";

    @MockitoBean
    private UserStateService userStateService;

    @BeforeEach
    void setUp() {
        mock_reportPermission(SEARCH_AND_VIEW_ACCOUNTS, true);
        mock_businessUnitPermission(10, true);
        mock_businessUnitPermission(20, true);
        mockCurrentUserBusinessUnits(
            List.of(
                businessUnitUserWithPermission("10", SEARCH_AND_VIEW_ACCOUNTS),
                businessUnitUserWithPermission("20", SEARCH_AND_VIEW_ACCOUNTS)
            )
        );
        mockBusinessUnitUsersForIds(
            List.of(10L),
            List.of(businessUnitUserWithPermission("10", SEARCH_AND_VIEW_ACCOUNTS))
        );
        mockBusinessUnitUsersForIds(
            List.of(20L),
            List.of(businessUnitUserWithPermission("20", SEARCH_AND_VIEW_ACCOUNTS))
        );
        mockBusinessUnitUsersForIds(
            List.of(10L, 20L),
            List.of(
                businessUnitUserWithPermission("10", SEARCH_AND_VIEW_ACCOUNTS),
                businessUnitUserWithPermission("20", SEARCH_AND_VIEW_ACCOUNTS)
            )
        );
    }

    @Nested
    class GetReportInstancesSadPath {

        @Test
        @JiraStory("PO-2251")
        @JiraEpic("PO-2248")
        void whenNoTokenPresent_unauthorizedIsReturned_sadPath() throws Exception {
            doThrow(new ResponseStatusException(UNAUTHORIZED, "Unauthorized"))
                .when(userStateService).checkAnyBusinessUnitUserHasPermission(SEARCH_AND_VIEW_ACCOUNTS);

            mockMvc.perform(get(URL_BASE)
                    .param("report_id", REPORT_ID))
                .andExpect(status().isUnauthorized());
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
            mock_reportPermission(SEARCH_AND_VIEW_ACCOUNTS, false);

            mockMvc.perform(get(URL_BASE)
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
            mock_businessUnitPermission(20, false);

            mockMvc.perform(get(URL_BASE)
                    .param("report_id", REPORT_ID)
                    .param("business_units", "20"))
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
            mockMvc.perform(get(URL_BASE)
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
            mockMvc.perform(get(URL_BASE)
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
            mockMvc.perform(get(URL_BASE)
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
            mockMvc.perform(get(URL_BASE)
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
            mockMvc.perform(get(URL_BASE)
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
            mockMvc.perform(get(URL_BASE)
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
            mockMvc.perform(get(URL_BASE)
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
            mockMvc.perform(get(URL_BASE)
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
            mockMvc.perform(get(URL_BASE)
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

    private void mock_reportPermission(FinesPermission permission, boolean permitted) {
        when(userStateService.checkAnyBusinessUnitUserHasPermission(permission)).thenReturn(permitted);
    }

    private void mock_businessUnitPermission(int businessUnitId, boolean permitted) {
        when(userStateService.isBusinessUnitPermittedForCurrentUser((short) businessUnitId)).thenReturn(permitted);
    }

    private void mockCurrentUserBusinessUnits(List<BusinessUnitUser> businessUnitUsers) {
        when(userStateService.getAllBusinessUnitUsersForCurrentUser()).thenReturn(businessUnitUsers);
    }

    private void mockBusinessUnitUsersForIds(List<Long> businessUnitIds, List<BusinessUnitUser> businessUnitUsers) {
        when(userStateService.getBusinessUnitUsersForBusinessUnitIds(businessUnitIds)).thenReturn(businessUnitUsers);
    }

    private BusinessUnitUser businessUnitUserWithPermission(String businessUnitId, FinesPermission permission) {
        return new BusinessUnitUser(
            "buUserId-1",
            Short.parseShort(businessUnitId),
            Set.of(permission.toUserPermission())
        );
    }
}
