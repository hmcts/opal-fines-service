package uk.gov.hmcts.opal.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus.REQUESTED;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceRequestReports;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceResponseReports;
import uk.gov.hmcts.opal.repository.ReportInstanceRepository;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.messaging.ReportQueuePublisherImpl;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.ReportInstanceControllerIntegrationTest")
@Sql(scripts = "classpath:db/insertData/insert_into_reports.sql", executionPhase = BEFORE_TEST_CLASS)
public class ReportInstancesControllerIntegrationTest extends AbstractIntegrationTest {
    private static final String URL_BASE = "/report-instances";
    private static final String REPORT_1BU_ID = "IT-report-1";
    private static final String REPORT_2BUs_ID = "IT-report-2";
    private static final String REPORT_NO_MANUAL_CREATION = "IT-report-3";
    private static final Long USER_ID = 41L;
    private static final String USER_NAME = "Mx User Person";

    @MockitoBean
    UserStateService userStateService;

    @MockitoBean
    private UserState userState;

    @MockitoBean
    private BusinessUnitUser businessUnitUser1;

    @MockitoBean
    private BusinessUnitUser businessUnitUser2;

    @MockitoBean
    private ReportQueuePublisherImpl reportQueuePublisher;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private ReportInstanceRepository reportInstanceRepository;

    @Test
    public void createReportInstance_singleBU() throws Exception {
        Mockito.when(userStateService.checkForAuthorisedUser(Mockito.anyString())).thenReturn(userState);
        Mockito.when(userState.getBusinessUnitUser()).thenReturn(Set.of(businessUnitUser1));
        Mockito.when(userState.getUserId()).thenReturn(USER_ID);
        Mockito.when(userState.getUserName()).thenReturn(USER_NAME);
        Mockito.when(businessUnitUser1.getBusinessUnitId()).thenReturn((short)1);

        CreateReportInstanceRequestReports request = CreateReportInstanceRequestReports.builder()
                .reportId(REPORT_1BU_ID)
                .reportName(null)
                .businessUnitIds(List.of(1))
                .reportParameters(Map.of(
                    "date-param", "2026-05-26",
                    "decimal-param", 5.0,
                    "integer-param", 5L,
                    "radio-param", List.of("one"),
                    "checkbox-param", List.of("one","two"),
                    "text-60-param", "value",
                    "text-100-param", "value",
                    "text-1000-param", "value"
                ))
                .build();

        String payload = objectMapper.writeValueAsString(request);
        log.info(":createReportInstance_singleBU payload: {}", payload);

        ResultActions resultActions = mockMvc.perform(
            post(URL_BASE).contentType(MediaType.APPLICATION_JSON).content(payload));


        // Assert
        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":createReportInstance_singleBU response:\n{}", ToJsonString.toPrettyJson(body));
        resultActions.andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));


        //verify data in db row saved correctly
        CreateReportInstanceResponseReports dto = objectMapper.readValue(body, CreateReportInstanceResponseReports.class);
        ReportInstanceEntity reportInstanceEntity = reportInstanceRepository
            .findById(dto.getReportInstanceId()).orElseThrow();
        assertEquals(REPORT_1BU_ID, reportInstanceEntity.getReport().getReportId());
        assertEquals(List.of(1), reportInstanceEntity.getBusinessUnit());
        assertEquals(USER_ID, reportInstanceEntity.getRequestedBy());
        assertEquals(USER_NAME, reportInstanceEntity.getRequestedByName());
        assertEquals(REQUESTED, reportInstanceEntity.getGenerationStatus());

        Mockito.verify(reportQueuePublisher).publish(dto.getReportInstanceId());
    }

    @Test
    public void createReportInstance_multiBUs() throws Exception {
        Mockito.when(userStateService.checkForAuthorisedUser(Mockito.anyString())).thenReturn(userState);
        Mockito.when(userState.getBusinessUnitUser()).thenReturn(Set.of(businessUnitUser1, businessUnitUser2));
        Mockito.when(userState.getUserId()).thenReturn(USER_ID);
        Mockito.when(userState.getUserName()).thenReturn(USER_NAME);
        Mockito.when(businessUnitUser1.getBusinessUnitId()).thenReturn((short)1);
        Mockito.when(businessUnitUser2.getBusinessUnitId()).thenReturn((short)2);

        CreateReportInstanceRequestReports request = CreateReportInstanceRequestReports.builder()
            .reportId(REPORT_2BUs_ID)
            .reportName(null)
            .businessUnitIds(List.of(1, 2))
            .reportParameters(new HashMap<>())
            .build();

        String payload = objectMapper.writeValueAsString(request);
        log.info(":createReportInstance_multiBUs payload: {}", payload);

        ResultActions resultActions = mockMvc.perform(
            post(URL_BASE).contentType(MediaType.APPLICATION_JSON).content(payload));


        // Assert
        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":createReportInstance_multiBUs response:\n{}", ToJsonString.toPrettyJson(body));
        resultActions.andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void createReportInstance_singleBU_fail2BUs_422() throws Exception {
        Mockito.when(userStateService.checkForAuthorisedUser(Mockito.anyString())).thenReturn(userState);
        Mockito.when(userState.getBusinessUnitUser()).thenReturn(Set.of(businessUnitUser1, businessUnitUser2));
        Mockito.when(businessUnitUser1.getBusinessUnitId()).thenReturn((short)1);
        Mockito.when(businessUnitUser2.getBusinessUnitId()).thenReturn((short)2);

        CreateReportInstanceRequestReports request = CreateReportInstanceRequestReports.builder()
            .reportId(REPORT_1BU_ID)
            .reportName(null)
            .businessUnitIds(List.of(1, 2))
            .reportParameters(new HashMap<>())
            .build();

        String payload = objectMapper.writeValueAsString(request);
        log.info(":createReportInstance_singleBU_fail2BUs payload: {}", payload);

        ResultActions resultActions = mockMvc.perform(
            post(URL_BASE).contentType(MediaType.APPLICATION_JSON).content(payload));

        resultActions.andExpect(status().isUnprocessableContent());
    }

    @Test
    public void createReportInstance_cannotManuallyCreate_422() throws Exception {
        Mockito.when(userStateService.checkForAuthorisedUser(Mockito.anyString())).thenReturn(userState);
        Mockito.when(userState.getBusinessUnitUser()).thenReturn(Set.of(businessUnitUser1));
        Mockito.when(businessUnitUser1.getBusinessUnitId()).thenReturn((short)1);

        CreateReportInstanceRequestReports request = CreateReportInstanceRequestReports.builder()
            .reportId(REPORT_NO_MANUAL_CREATION)
            .reportName(null)
            .businessUnitIds(List.of(1))
            .reportParameters(new HashMap<>())
            .build();

        String payload = objectMapper.writeValueAsString(request);
        log.info(":createReportInstance_cannotManuallyCreate payload: {}", payload);

        ResultActions resultActions = mockMvc.perform(
            post(URL_BASE).contentType(MediaType.APPLICATION_JSON).content(payload));
        resultActions.andExpect(status().isUnprocessableContent());
    }


    @Test
    public void createReportInstance_wrongBU_403() throws Exception {
        Mockito.when(userStateService.checkForAuthorisedUser(Mockito.anyString())).thenReturn(userState);
        Mockito.when(userState.getBusinessUnitUser()).thenReturn(Set.of(businessUnitUser1));
        Mockito.when(businessUnitUser1.getBusinessUnitId()).thenReturn((short)1);

        CreateReportInstanceRequestReports request = CreateReportInstanceRequestReports.builder()
            .reportId(REPORT_1BU_ID)
            .reportName(null)
            .businessUnitIds(List.of(2))
            .reportParameters(new HashMap<>())
            .build();

        String payload = objectMapper.writeValueAsString(request);
        log.info(":createReportInstance_wrongBU payload: {}", payload);

        ResultActions resultActions = mockMvc.perform(
            post(URL_BASE).contentType(MediaType.APPLICATION_JSON).content(payload));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    public void createReportInstance_notAllBUs_403() throws Exception {
        Mockito.when(userStateService.checkForAuthorisedUser(Mockito.anyString())).thenReturn(userState);
        Mockito.when(userState.getBusinessUnitUser()).thenReturn(Set.of(businessUnitUser1));
        Mockito.when(businessUnitUser1.getBusinessUnitId()).thenReturn((short)1);

        CreateReportInstanceRequestReports request = CreateReportInstanceRequestReports.builder()
            .reportId(REPORT_2BUs_ID)
            .reportName(null)
            .businessUnitIds(List.of(1, 2))
            .reportParameters(new HashMap<>())
            .build();

        String payload = objectMapper.writeValueAsString(request);
        log.info(":createReportInstance_wrongBU payload: {}", payload);

        ResultActions resultActions = mockMvc.perform(
            post(URL_BASE).contentType(MediaType.APPLICATION_JSON).content(payload));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    public void createReportInstance_reportIDNotFound_404() throws Exception {
        CreateReportInstanceRequestReports request = CreateReportInstanceRequestReports.builder()
            .reportId("unknown-report-id")
            .reportName(null)
            .businessUnitIds(List.of(1))
            .reportParameters(new HashMap<>())
            .build();

        String payload = objectMapper.writeValueAsString(request);
        log.info(":createReportInstance_reportIDNotFound_404 payload: {}", payload);

        ResultActions resultActions = mockMvc.perform(
            post(URL_BASE).contentType(MediaType.APPLICATION_JSON).content(payload));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":createReportInstance_reportIDNotFound_404 response:\n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"))
            .andExpect(jsonPath("$.title").value("Entity Not Found"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("The requested entity could not be found"))
            .andExpect(jsonPath("$.retriable").value(false));
    }

    @Test
    public void createReportInstance_missingBUs_400() throws Exception {
        String payload = """
            {
              "report_id": "%s",
              "report_name": null,
              "Business_unit_ids: null,
              "report_parameters": {}
            }
            """.formatted(REPORT_1BU_ID);

        log.info(":createReportInstance_missingBUs_400 payload: {}", payload);

        ResultActions resultActions = mockMvc.perform(
            post(URL_BASE).contentType(MediaType.APPLICATION_JSON).content(payload));

        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    public void createReportInstance_malformedRequest_400() throws Exception {
        String payload = """
            {
              "report_id": "%s",
              "report_name": null,
              "business_unit_ids": 1,
              "report_parameters": {}
            }
            """.formatted(REPORT_1BU_ID);

        log.info(":createReportInstance_malformedRequest payload: {}", payload);

        ResultActions resultActions = mockMvc.perform(
            post(URL_BASE).contentType(MediaType.APPLICATION_JSON).content(payload));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":createReportInstance_malformedRequest response:\n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/message-not-readable"))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").value(
                "The request body could not be read. It may be missing or invalid JSON."))
            .andExpect(jsonPath("$.retriable").value(false));
    }

    @Test
    public void createReportInstance_publishFail() throws Exception{
        Mockito.doThrow(new IllegalArgumentException("Unable to publish report queue message"))
            .when(reportQueuePublisher).publish(anyLong());

        Mockito.when(userStateService.checkForAuthorisedUser(Mockito.anyString())).thenReturn(userState);
        Mockito.when(userState.getBusinessUnitUser()).thenReturn(Set.of(businessUnitUser1));
        Mockito.when(userState.getUserId()).thenReturn(USER_ID);
        Mockito.when(userState.getUserName()).thenReturn(USER_NAME);
        Mockito.when(businessUnitUser1.getBusinessUnitId()).thenReturn((short)1);

        CreateReportInstanceRequestReports request = CreateReportInstanceRequestReports.builder()
            .reportId(REPORT_1BU_ID)
            .reportName(null)
            .businessUnitIds(List.of(1))
            .reportParameters(Map.of(
                "date-param", "2026-05-26",
                "decimal-param", 5.0,
                "integer-param", 5L,
                "radio-param", List.of("one"),
                "checkbox-param", List.of("one","two"),
                "text-60-param", "value",
                "text-100-param", "value",
                "text-1000-param", "value"
            ))
            .build();

        String payload = objectMapper.writeValueAsString(request);
        log.info(":createReportInstance_publishFail payload: {}", payload);

        Long tableSizeBefore = reportInstanceRepository.count();

        ResultActions resultActions = mockMvc.perform(
            post(URL_BASE).contentType(MediaType.APPLICATION_JSON).content(payload));


        Long tableSizeAfter = reportInstanceRepository.count();
        log.info("Before: {}, After: {}", tableSizeBefore, tableSizeAfter);


        // Assert
        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":createReportInstance_publishFail response:\n{}", ToJsonString.toPrettyJson(body));
        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    public void createReportInstance_reportParameterValidation_mandatoryFieldsNotSuppliedFail() throws Exception {
        Mockito.when(userStateService.checkForAuthorisedUser(Mockito.anyString())).thenReturn(userState);
        Mockito.when(userState.getBusinessUnitUser()).thenReturn(Set.of(businessUnitUser1));
        Mockito.when(userState.getUserId()).thenReturn(USER_ID);
        Mockito.when(userState.getUserName()).thenReturn(USER_NAME);
        Mockito.when(businessUnitUser1.getBusinessUnitId()).thenReturn((short)1);

        CreateReportInstanceRequestReports request = CreateReportInstanceRequestReports.builder()
            .reportId(REPORT_1BU_ID)
            .reportName(null)
            .businessUnitIds(List.of(1))
            .reportParameters(null)
            .build();

        String payload = objectMapper.writeValueAsString(request);
        log.info(":createReportInstance_reportParameterValidation_mandatoryFieldsNotSuppliedFail payload: {}", payload);

        ResultActions resultActions = mockMvc.perform(
            post(URL_BASE).contentType(MediaType.APPLICATION_JSON).content(payload));


        // Assert
        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":createReportInstance_reportParameterValidation_mandatoryFieldsNotSuppliedFail response:\n{}", ToJsonString.toPrettyJson(body));
        resultActions.andExpect(status().isUnprocessableContent());
    }
}
