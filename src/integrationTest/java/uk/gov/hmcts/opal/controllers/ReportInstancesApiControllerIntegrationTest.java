package uk.gov.hmcts.opal.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.SEARCH_AND_VIEW_ACCOUNTS;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;

import jakarta.servlet.ServletException;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.common.exceptions.standard.UnauthorizedException;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.generated.model.ReportInstanceListReportsInner;
import uk.gov.hmcts.opal.service.report.GenericReportService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

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
import uk.gov.hmcts.opal.common.user.authorisation.client.service.UserStateClientService;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.generated.model.ReportReferenceReports.SupportedFileTypesEnum;
import uk.gov.hmcts.opal.generated.model.StatusReports.CodeEnum;
import uk.gov.hmcts.opal.service.UserStateService;

    private MockHttpServletRequestBuilder authorisedGet() {
        return get(URL_BASE)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header("authorization", userStateStub.getBearerToken());
    }
@ActiveProfiles({"integration"})
@Sql(scripts = "classpath:db/insertData/insert_into_report_instances_entity_graph.sql", executionPhase = BEFORE_TEST_CLASS)
@Slf4j(topic = "opal.ReportInstanceIntegrationTests")
@DisplayName("ReportInstancesApiController Integration Tests")
public class ReportInstancesApiControllerIntegrationTest extends AbstractIntegrationTest {
    private static final String REPORT_INSTANCE_URL_BASE = "/report-instances";
    private static final int REPORT_INSTANCE_ID_READY = 123;
    private static final int REPORT_INSTANCE_ID_REQUESTED = 234;
    private static final int REPORT_INSTANCE_ID_IN_PROGRESS = 345;
    private static final int REPORT_INSTANCE_ID_ERROR = 400;
    private static final int REPORT_INSTANCE_ID_NO_SUPPORTED_TYPES = 567;
    private static final short BU_ID_1 = 1;
    private static final short BU_ID_2_WELSH_LANGUAGE = 2;

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
    private static final LocalDateTime REQUESTED_AT = LocalDateTime.of(2026, 5, 10, 17, 30, 0);
    private static final LocalDateTime GENERATED_AT = LocalDateTime.of(2026, 5, 11, 17, 30, 0);
    private static final LocalDateTime DELETION_AT = LocalDateTime.of(2026, 5, 25, 17, 30, 0);

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
    @MockitoBean
    UserStateService userStateService;

    @Nested
    class GetReportInstancesSadPath {
    @MockitoBean
    UserStateClientService userStateClientService;

        @Test
        @JiraStory("PO-2251")
        @JiraEpic("PO-2248")
        void whenNoTokenPresent_unauthorizedIsReturned_sadPath() {
            ServletException exception = assertThrows(
                ServletException.class,
                () -> mockMvc.perform(get(URL_BASE).param("report_id", REPORT_ID))
            );
    @MockitoBean
    UserState userState;

            assertThat(exception.getCause())
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Current user is not authenticated with OpalJwtAuthenticationToken");
        }
    @MockitoBean
    BusinessUnitUser buUser1;

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
    @MockitoBean
    BusinessUnitUser buUser2;

        @Test
        @JiraStory("PO-2251")
        @JiraEpic("PO-2248")
        void whenInProgressInstanceReturned_isNotDownloadable_happyPath() throws Exception {
            doReturn(List.of(inProgressDto())).when(genericReportService).searchReportInstances(
                java.time.LocalDate.of(2026, 2, 1),
                java.time.LocalDate.of(2026, 2, 28),
                null,
                null,
                REPORT_ID
            );
    @Test
    void getReportInstance_success_singleBUInstance() throws Exception {
        Mockito.when(userStateService.checkForAuthorisedUser(any())).thenReturn(userState);
        Mockito.when(userState.getBusinessUnitUser()).thenReturn(Set.of(buUser1));
        Mockito.when(buUser1.getBusinessUnitId()).thenReturn(BU_ID_1);

        ResultActions result = mockMvc.perform(
            get(REPORT_INSTANCE_URL_BASE + "/" + REPORT_INSTANCE_ID_READY)
                .contentType(MediaType.APPLICATION_JSON)
                .header("authorization", "Bearer some_value"));

        // Assert
        String body = result.andReturn().getResponse().getContentAsString();
        log.info(":testGetReportInstance response:\n{}", ToJsonString.toPrettyJson(body));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.instance_id").value(REPORT_INSTANCE_ID_READY))
            .andExpect(jsonPath("$.requested_at").value("2026-05-10T17:30:00"))
            .andExpect(jsonPath("$.generated_at").value("2026-05-11T17:30:00"))
            .andExpect(jsonPath("$.requested_by.user_id").value(1001))
            .andExpect(jsonPath("$.requested_by.name").value("Report Person"))
            .andExpect(jsonPath("$.name").value("Operational report: single BU"))

            .andExpect(jsonPath("$.business_units[0].business_unit_id").value("1"))
            .andExpect(jsonPath("$.business_units[0].business_unit_name").value("BU no1"))
            .andExpect(jsonPath("$.business_units[0].welsh_speaking").value("N"))

            .andExpect(jsonPath("$.status.code").value(CodeEnum.READY.name()))
            .andExpect(jsonPath("$.status.display_name").value(CodeEnum.READY.getValue()))
            .andExpect(jsonPath("$.number_of_records").value(10))
            .andExpect(jsonPath("$.is_downloadable").value(true))
            .andExpect(jsonPath("$.errors").value(IsNull.nullValue()))

            .andExpect(jsonPath("$.report_parameters.param1").value("A string parameter value"))
            .andExpect(jsonPath("$.report_parameters.param2").value(987))

            .andExpect(jsonPath("$.retain_until").value("2026-05-25T17:30:00"))
            .andExpect(jsonPath("$.report.id").value("full_report_single_bu"))
            .andExpect(jsonPath("$.report.supported_file_types").value(
                Matchers.contains(SupportedFileTypesEnum.CSV.name(), SupportedFileTypesEnum.PDF.name(),
                    SupportedFileTypesEnum.XML.name()/*, SupportedFileTypesEnum.JSON.name()*/)));
    }

    @Test
    void getReportInstance_success_multiBUInstance() throws Exception {
        Mockito.when(userStateService.checkForAuthorisedUser(any())).thenReturn(userState);
        Mockito.when(userState.getBusinessUnitUser()).thenReturn(Set.of(buUser1, buUser2));
        Mockito.when(buUser1.getBusinessUnitId()).thenReturn(BU_ID_1);
        Mockito.when(buUser2.getBusinessUnitId()).thenReturn(BU_ID_2_WELSH_LANGUAGE);

        ResultActions result = mockMvc.perform(
            get(REPORT_INSTANCE_URL_BASE + "/" + REPORT_INSTANCE_ID_IN_PROGRESS)
                .contentType(MediaType.APPLICATION_JSON)
                .header("authorization", "Bearer some_value"));

        String body = result.andReturn().getResponse().getContentAsString();
        log.info(":getReportInstance_success_multiBUInstance response:\n{}", ToJsonString.toPrettyJson(body));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.instance_id").value(REPORT_INSTANCE_ID_IN_PROGRESS))
            .andExpect(jsonPath("$.business_units").value(Matchers.hasSize(2)))

            .andExpect(jsonPath("$.business_units[0].business_unit_id").value("1"))
            .andExpect(jsonPath("$.business_units[0].business_unit_name").value("BU no1"))
            .andExpect(jsonPath("$.business_units[0].welsh_speaking").value("N"))
            .andExpect(jsonPath("$.business_units[1].business_unit_id").value("2"))
            .andExpect(jsonPath("$.business_units[1].business_unit_name").value("BU no2 - Welsh"))
            .andExpect(jsonPath("$.business_units[1].welsh_speaking").value("Y"))

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
            .andExpect(jsonPath("$.status.code").value(CodeEnum.IN_PROGRESS.name()))
            .andExpect(jsonPath("$.status.display_name").value(CodeEnum.IN_PROGRESS.getValue()))
            .andExpect(jsonPath("$.is_downloadable").value(false));
    }

        @Test
        @JiraStory("PO-2251")
        @JiraEpic("PO-2248")
        void whenInstanceNameNull_reportTitleIsUsed_happyPath() throws Exception {
            doReturn(List.of(inProgressDto())).when(genericReportService).searchReportInstances(
                java.time.LocalDate.of(2026, 2, 1),
                java.time.LocalDate.of(2026, 2, 28),
                null,
                null,
                REPORT_ID
            );
    void getReportInstance_success_useReportInstanceNameOverride() {
    @Test
    void getReportInstance_success_useReportInstanceNameOverride() throws Exception {
        Mockito.when(userStateService.checkForAuthorisedUser(any())).thenReturn(userState);
        Mockito.when(userState.getBusinessUnitUser()).thenReturn(Set.of(buUser1, buUser2));
        Mockito.when(buUser1.getBusinessUnitId()).thenReturn(BU_ID_1);
        Mockito.when(buUser2.getBusinessUnitId()).thenReturn(BU_ID_2_WELSH_LANGUAGE);

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
        ResultActions result = mockMvc.perform(
            get(REPORT_INSTANCE_URL_BASE + "/" + REPORT_INSTANCE_ID_IN_PROGRESS)
                .contentType(MediaType.APPLICATION_JSON)
                .header("authorization", "Bearer some_value"));

        String body = result.andReturn().getResponse().getContentAsString();
        log.info(":getReportInstance_success_useReportInstanceNameOverride response:\n{}",
                 ToJsonString.toPrettyJson(body));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.instance_id").value(REPORT_INSTANCE_ID_IN_PROGRESS))
            .andExpect(jsonPath("$.name").value("Report instance name override"))
            .andExpect(jsonPath("$.report.id").value("full_report_multi_bus"));
    }

        @Test
        @JiraStory("PO-2251")
        @JiraEpic("PO-2248")
        void whenNoMatchingInstances_emptyArrayReturned_happyPath() throws Exception {
            doReturn(List.of()).when(genericReportService).searchReportInstances(
                java.time.LocalDate.of(2020, 2, 1),
                java.time.LocalDate.of(2020, 2, 28),
                null,
                null,
                REPORT_ID
            );
    //INT.04 covered 1
    //INT.05 covered 1-4

    @Test
    void getReportInstance_success_butReportInstanceDataHasErrors() throws Exception {
        Mockito.when(userStateService.checkForAuthorisedUser(any())).thenReturn(userState);
        Mockito.when(userState.getBusinessUnitUser()).thenReturn(Set.of(buUser1));
        Mockito.when(buUser1.getBusinessUnitId()).thenReturn(BU_ID_1);

        ResultActions result = mockMvc.perform(
            get(REPORT_INSTANCE_URL_BASE + "/" + REPORT_INSTANCE_ID_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .header("authorization", "Bearer some_value"));

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
        String body = result.andReturn().getResponse().getContentAsString();
        log.info(":getReportInstance_success_butReportInstanceDataHasErrors response:\n{}",
                 ToJsonString.toPrettyJson(body));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.instance_id").value(REPORT_INSTANCE_ID_ERROR))
            .andExpect(jsonPath("$.status.code").value(CodeEnum.ERROR.name()))
            .andExpect(jsonPath("$.status.display_name").value(CodeEnum.ERROR.getValue()))
            .andExpect(jsonPath("$.is_downloadable").value(false))
            .andExpect(jsonPath("$.errors").value(Matchers.hasSize(1)))
            .andExpect(jsonPath("$.errors[0].operationId").value("ERROR-ID"))
            .andExpect(jsonPath("$.errors[0].error").value("Generation failed"));
    }

    @Test
    void getReportInstance_success_allSupportedTypes() throws Exception {
        Mockito.when(userStateService.checkForAuthorisedUser(any())).thenReturn(userState);
        Mockito.when(userState.getBusinessUnitUser()).thenReturn(Set.of(buUser1));
        Mockito.when(buUser1.getBusinessUnitId()).thenReturn(BU_ID_1);

        ResultActions result = mockMvc.perform(
            get(REPORT_INSTANCE_URL_BASE + "/" + REPORT_INSTANCE_ID_READY)
                .contentType(MediaType.APPLICATION_JSON)
                .header("authorization", "Bearer some_value"));

        String body = result.andReturn().getResponse().getContentAsString();
        log.info(":getReportInstance_success_allSupportedTypes response:\n{}", ToJsonString.toPrettyJson(body));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.instance_id").value(REPORT_INSTANCE_ID_READY))
            .andExpect(jsonPath("$.report.supported_file_types").value(
                Matchers.containsInAnyOrder(SupportedFileTypesEnum.CSV.name(), SupportedFileTypesEnum.PDF.name(),
                    SupportedFileTypesEnum.XML.name()))); //todo add JSON
    }

    @Test
    void getReportInstance_success_reportParameters() throws Exception {
        Mockito.when(userStateService.checkForAuthorisedUser(any())).thenReturn(userState);
        Mockito.when(userState.getBusinessUnitUser()).thenReturn(Set.of(buUser1));
        Mockito.when(buUser1.getBusinessUnitId()).thenReturn(BU_ID_1);

        ResultActions result = mockMvc.perform(
            get(REPORT_INSTANCE_URL_BASE + "/" + REPORT_INSTANCE_ID_READY)
                .contentType(MediaType.APPLICATION_JSON)
                .header("authorization", "Bearer some_value"));

        String body = result.andReturn().getResponse().getContentAsString();
        log.info(":getReportInstance_success_reportParameters response:\n{}", ToJsonString.toPrettyJson(body));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.instance_id").value(REPORT_INSTANCE_ID_READY))
            .andExpect(jsonPath("$.report_parameters.param1").value("A string parameter value"))
            .andExpect(jsonPath("$.report_parameters.param2").value(987));
    }

    //INT.09 covered 1

    @Test
    void getReportInstance_success_notReady_notDownloadable() throws Exception {
        Mockito.when(userStateService.checkForAuthorisedUser(any())).thenReturn(userState);
        Mockito.when(userState.getBusinessUnitUser()).thenReturn(Set.of(buUser1));
        Mockito.when(buUser1.getBusinessUnitId()).thenReturn(BU_ID_1);

        ResultActions result = mockMvc.perform(
            get(REPORT_INSTANCE_URL_BASE + "/" + REPORT_INSTANCE_ID_REQUESTED)
                .contentType(MediaType.APPLICATION_JSON)
                .header("authorization", "Bearer some_value"));

        String body = result.andReturn().getResponse().getContentAsString();
        log.info(":getReportInstance_success_notReady_notDownloadable response:\n{}",
                 ToJsonString.toPrettyJson(body));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.instance_id").value(REPORT_INSTANCE_ID_REQUESTED))
            .andExpect(jsonPath("$.generated_at").value(IsNull.nullValue()))
            .andExpect(jsonPath("$.status.code").value(CodeEnum.REQUESTED.name()))
            .andExpect(jsonPath("$.status.display_name").value(CodeEnum.REQUESTED.getValue()))
            .andExpect(jsonPath("$.is_downloadable").value(false))
            .andExpect(jsonPath("$.report_parameters").value(IsNull.nullValue()));
    }

    @Test
    void getReportInstance_success_readyNoTypes_notDownloadable() throws Exception {
        Mockito.when(userStateService.checkForAuthorisedUser(any())).thenReturn(userState);
        Mockito.when(userState.getBusinessUnitUser()).thenReturn(Set.of(buUser1));
        Mockito.when(buUser1.getBusinessUnitId()).thenReturn(BU_ID_1);

        ResultActions result = mockMvc.perform(
            get(REPORT_INSTANCE_URL_BASE + "/" + REPORT_INSTANCE_ID_NO_SUPPORTED_TYPES)
                .contentType(MediaType.APPLICATION_JSON)
                .header("authorization", "Bearer some_value"));

        String body = result.andReturn().getResponse().getContentAsString();
        log.info(":getReportInstance_success_readyNoTypes_notDownloadable response:\n{}",
                 ToJsonString.toPrettyJson(body));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.instance_id").value(REPORT_INSTANCE_ID_NO_SUPPORTED_TYPES))
            .andExpect(jsonPath("$.status.code").value(CodeEnum.READY.name()))
            .andExpect(jsonPath("$.status.display_name").value(CodeEnum.READY.getValue()))
            .andExpect(jsonPath("$.is_downloadable").value(false))
            .andExpect(jsonPath("$.report.supported_file_types").value(Matchers.empty()));
    }

    //INT.12 covered 1

    @Test
    void getReportInstance_401_noToken() throws Exception {
        Mockito.doThrow(new ResponseStatusException(UNAUTHORIZED, "Unauthorized"))
            .when(userStateService).checkForAuthorisedUser(any());

        ResultActions result = mockMvc.perform(
            get(REPORT_INSTANCE_URL_BASE + "/" + REPORT_INSTANCE_ID_READY)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isUnauthorized());
    }

    @Test
    void getReportInstance_403_incorrectBUs() throws Exception {
        Mockito.when(userStateService.checkForAuthorisedUser(any())).thenReturn(userState);
        Mockito.when(userState.getBusinessUnitUser()).thenReturn(Set.of(buUser1));
        Mockito.when(buUser1.getBusinessUnitId()).thenReturn(BU_ID_1);

        ResultActions result = mockMvc.perform(
            get(REPORT_INSTANCE_URL_BASE + "/" + REPORT_INSTANCE_ID_IN_PROGRESS)
                .contentType(MediaType.APPLICATION_JSON)
                .header("authorization", "Bearer some_value"));

        String body = result.andReturn().getResponse().getContentAsString();
        log.info(":getReportInstance_403_incorrectBUs response:\n{}", ToJsonString.toPrettyJson(body));

        result.andExpect(status().isForbidden())
            .andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.detail").value("You do not have permission to access this resource"))
            .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void getReportInstance_404_reportInstanceNotFound() throws Exception {
        Mockito.when(userStateService.checkForAuthorisedUser(any())).thenReturn(userState);
        Mockito.when(userState.getBusinessUnitUser()).thenReturn(Set.of(buUser1));
        Mockito.when(buUser1.getBusinessUnitId()).thenReturn(BU_ID_1);

        ResultActions result = mockMvc.perform(
            get(REPORT_INSTANCE_URL_BASE + "/-1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("authorization", "Bearer some_value"));

        String body = result.andReturn().getResponse().getContentAsString();
        log.info(":getReportInstance_404_reportInstanceNotFound response:\n{}", ToJsonString.toPrettyJson(body));

        result.andExpect(status().isNotFound())
            .andExpect(jsonPath("$.title").value("Entity Not Found"))
            .andExpect(jsonPath("$.detail").value("The requested entity could not be found"))
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getReportInstance_attempt406() throws Exception {
        ResultActions result = mockMvc.perform(
            get(REPORT_INSTANCE_URL_BASE + "/" + "NOT_A_NUMBER")
                .header("authorization", "Bearer some_value"));

        result.andExpect(status().isNotAcceptable());
    }

    //INT.17
    //INT.18
    //INT.19
}
