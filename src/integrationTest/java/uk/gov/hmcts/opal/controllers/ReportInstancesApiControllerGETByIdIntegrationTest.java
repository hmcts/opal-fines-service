package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.server.ResponseStatusException;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.generated.model.ReportReferenceReports.SupportedFileTypesEnum;
import uk.gov.hmcts.opal.generated.model.StatusReports.CodeEnum;
import uk.gov.hmcts.opal.service.UserStateService;

@ActiveProfiles({"integration"})
@Sql(
    scripts = "classpath:db/insertData/insert_into_report_instances_entity_graph.sql",
    executionPhase = BEFORE_TEST_CLASS)
@Sql(
    scripts = "classpath:db/deleteData/delete_from_report_instances_entity_graph.sql",
    executionPhase = AFTER_TEST_CLASS)
@Slf4j(topic = "opal.ReportInstanceGetByIdIntegrationTests")
@DisplayName("ReportInstancesApiController GET By Id Integration Tests")
public class ReportInstancesApiControllerGETByIdIntegrationTest extends AbstractIntegrationTest {

    private static final String REPORT_INSTANCE_URL_BASE = "/report-instances";
    private static final int REPORT_INSTANCE_ID_READY = 123;
    private static final int REPORT_INSTANCE_ID_REQUESTED = 234;
    private static final int REPORT_INSTANCE_ID_IN_PROGRESS = 345;
    private static final int REPORT_INSTANCE_ID_ERROR = 400;
    private static final int REPORT_INSTANCE_ID_NO_SUPPORTED_TYPES = 567;
    private static final short BU_ID_1 = 1;
    private static final short BU_ID_2_WELSH_LANGUAGE = 2;

    @MockitoBean
    UserStateService userStateService;

    @MockitoBean
    UserState userState;

    @MockitoBean
    BusinessUnitUser buUser1;

    @MockitoBean
    BusinessUnitUser buUser2;

    @Test
    @JiraStory("PO-2254")
    @JiraEpic("PO-2248")
    @JiraTestKey("PO-8275")
    void getReportInstance_success_singleBUInstance() throws Exception {
        Mockito.when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        Mockito.when(userState.getBusinessUnitUser()).thenReturn(Set.of(buUser1));
        Mockito.when(buUser1.getBusinessUnitId()).thenReturn(BU_ID_1);

        ResultActions result = mockMvc.perform(
            get(REPORT_INSTANCE_URL_BASE + "/" + REPORT_INSTANCE_ID_READY)
                .contentType(MediaType.APPLICATION_JSON)
                .header("authorization", "Bearer some_value"));

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
                    SupportedFileTypesEnum.XML.name(), SupportedFileTypesEnum.JSON.name())));
    }

    @Test
    @JiraStory("PO-2254")
    @JiraEpic("PO-2248")
    @JiraTestKey("PO-8280")
    void getReportInstance_success_multiBUInstance() throws Exception {
        Mockito.when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
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
            .andExpect(jsonPath("$.status.code").value(CodeEnum.IN_PROGRESS.name()))
            .andExpect(jsonPath("$.status.display_name").value(CodeEnum.IN_PROGRESS.getValue()))
            .andExpect(jsonPath("$.is_downloadable").value(false));
    }

    @Test
    @JiraStory("PO-2254")
    @JiraEpic("PO-2248")
    @JiraTestKey("PO-8279")
    void getReportInstance_success_useReportInstanceNameOverride() throws Exception {
        Mockito.when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        Mockito.when(userState.getBusinessUnitUser()).thenReturn(Set.of(buUser1, buUser2));
        Mockito.when(buUser1.getBusinessUnitId()).thenReturn(BU_ID_1);
        Mockito.when(buUser2.getBusinessUnitId()).thenReturn(BU_ID_2_WELSH_LANGUAGE);

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
    @JiraStory("PO-2254")
    @JiraEpic("PO-2248")
    @JiraTestKey("PO-8272")
    void getReportInstance_success_butReportInstanceDataHasErrors() throws Exception {
        Mockito.when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        Mockito.when(userState.getBusinessUnitUser()).thenReturn(Set.of(buUser1));
        Mockito.when(buUser1.getBusinessUnitId()).thenReturn(BU_ID_1);

        ResultActions result = mockMvc.perform(
            get(REPORT_INSTANCE_URL_BASE + "/" + REPORT_INSTANCE_ID_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .header("authorization", "Bearer some_value"));

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
    @JiraStory("PO-2254")
    @JiraEpic("PO-2248")
    @JiraTestKey("PO-8273")
    void getReportInstance_success_allSupportedTypes() throws Exception {
        Mockito.when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
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
                    SupportedFileTypesEnum.XML.name(), SupportedFileTypesEnum.JSON.name())));
    }

    @Test
    @JiraStory("PO-2254")
    @JiraEpic("PO-2248")
    @JiraTestKey("PO-8276")
    void getReportInstance_success_reportParameters() throws Exception {
        Mockito.when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
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

    @Test
    @JiraStory("PO-2254")
    @JiraEpic("PO-2248")
    @JiraTestKey("PO-8281")
    void getReportInstance_success_notReady_notDownloadable() throws Exception {
        Mockito.when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
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
            .andExpect(jsonPath("$.generated_at").value(nullValue()))
            .andExpect(jsonPath("$.status.code").value(CodeEnum.REQUESTED.name()))
            .andExpect(jsonPath("$.status.display_name").value(CodeEnum.REQUESTED.getValue()))
            .andExpect(jsonPath("$.is_downloadable").value(false))
            .andExpect(jsonPath("$.report_parameters").value(Matchers.anEmptyMap()));
    }

    @Test
    @JiraStory("PO-2254")
    @JiraEpic("PO-2248")
    @JiraTestKey("PO-8277")
    void getReportInstance_success_readyNoTypes_notDownloadable() throws Exception {
        Mockito.when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
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

    @Test
    @JiraStory("PO-2254")
    @JiraEpic("PO-2248")
    @JiraTestKey("PO-8270")
    void getReportInstance_401_noToken() throws Exception {
        Mockito.doThrow(new ResponseStatusException(UNAUTHORIZED, "Unauthorized"))
            .when(userStateService).getUserStateV1FromSecurityContext();

        ResultActions result = mockMvc.perform(
            get(REPORT_INSTANCE_URL_BASE + "/" + REPORT_INSTANCE_ID_READY)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isUnauthorized());
    }

    @Test
    @JiraStory("PO-2254")
    @JiraEpic("PO-2248")
    @JiraTestKey("PO-8274")
    void getReportInstance_403_incorrectBUs() throws Exception {
        Mockito.when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
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
    @JiraStory("PO-2254")
    @JiraEpic("PO-2248")
    @JiraTestKey("PO-8271")
    void getReportInstance_404_reportInstanceNotFound() throws Exception {
        Mockito.when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
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
    @JiraStory("PO-2254")
    @JiraEpic("PO-2248")
    @JiraTestKey("PO-8278")
    void getReportInstance_attempt406() throws Exception {
        ResultActions result = mockMvc.perform(
            get(REPORT_INSTANCE_URL_BASE + "/" + "NOT_A_NUMBER")
                .header("authorization", "Bearer some_value"));

        result.andExpect(status().isNotAcceptable());
    }

}
