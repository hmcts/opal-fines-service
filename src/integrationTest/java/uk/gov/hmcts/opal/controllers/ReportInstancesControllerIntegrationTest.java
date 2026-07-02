package uk.gov.hmcts.opal.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.ReportInstanceControllerIntegrationTest")
@Sql(scripts = "classpath:db/insertData/insert_into_reports.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_reports.sql", executionPhase = AFTER_TEST_CLASS)
public class ReportInstancesControllerIntegrationTest extends AbstractIntegrationTest {
    private static final String URL_BASE = "/report-instances";
    private static final String REPORT_1BU_ID = "IT-report-1";
    private static final String REPORT_2BUs_ID = "IT-report-2";
    private static final String REPORT_NO_MANUAL_CREATION = "it_report_full";
    private static final Long USER_ID = 41L;
    private static final String USER_NAME = "Mx User Person";

    @MockitoBean
    UserStateService userStateService;

    @MockitoBean
    private ReportQueuePublisherImpl reportQueuePublisher;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private ReportInstanceRepository reportInstanceRepository;

    @Test
    @DisplayName("create report instance single business unit")
    @JiraStory("PO-2252")
    @JiraEpic("PO-2248")
    @JiraTestKey("PO-7742")
    void createReportInstance_singleBU() throws Exception {
        givenUserStateForBusinessUnits((short)1);
        Map<String, Object> parameterMap = Map.of(
            "date-param", "2026-05-26",
            "decimal-param", 5.0,
            "integer-param", 5,
            "radio-param", List.of("one"),
            "checkbox-param", List.of("one","two"),
            "text-60-param", "value",
            "text-100-param", "value",
            "text-1000-param", "value"
        );
        CreateReportInstanceRequestReports request = CreateReportInstanceRequestReports.builder()
                .reportId(REPORT_1BU_ID)
                .reportName(null)
                .businessUnitIds(List.of(1))
                .reportParameters(parameterMap)
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
        CreateReportInstanceResponseReports dto = objectMapper.readValue(body,
            CreateReportInstanceResponseReports.class);
        ReportInstanceEntity reportInstanceEntity = reportInstanceRepository
            .findById(dto.getReportInstanceId()).orElseThrow();
        assertEquals(REPORT_1BU_ID, reportInstanceEntity.getReport().getReportId());
        assertEquals(List.of(1), reportInstanceEntity.getBusinessUnit());
        assertEquals(USER_ID, reportInstanceEntity.getRequestedBy());
        assertEquals(USER_NAME, reportInstanceEntity.getRequestedByName());
        assertEquals(REQUESTED, reportInstanceEntity.getGenerationStatus());

        Map dtoParameters = objectMapper.readValue(reportInstanceEntity.getReportParameters(), Map.class);
        assertEquals(parameterMap.get("date-param"), dtoParameters.get("date-param"));
        assertEquals(parameterMap.get("decimal-param"), dtoParameters.get("decimal-param"));
        assertEquals(parameterMap.get("integer-param"), dtoParameters.get("integer-param"));
        assertEquals(parameterMap.get("radio-param"), dtoParameters.get("radio-param"));
        assertEquals(parameterMap.get("checkbox-param"), dtoParameters.get("checkbox-param"));
        assertEquals(parameterMap.get("text-60-param"), dtoParameters.get("text-60-param"));
        assertEquals(parameterMap.get("text-100-param"), dtoParameters.get("text-100-param"));
        assertEquals(parameterMap.get("text-1000-param"), dtoParameters.get("text-1000-param"));

        verify(reportQueuePublisher).publish(dto.getReportInstanceId());
    }

    @Test
    @JiraStory("PO-2252")
    @JiraEpic("PO-2248")
    @JiraTestKey("PO-7744")
    void createReportInstance_multiBUs() throws Exception {
        givenUserStateForBusinessUnits((short)1, (short)2);

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
    @JiraStory("PO-2252")
    @JiraEpic("PO-2248")
    @JiraTestKey("PO-7746")
    void createReportInstance_singleBU_fail2BUs_422() throws Exception {
        givenUserStateForBusinessUnits((short)1, (short)2);

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
    @JiraStory("PO-2252")
    @JiraEpic("PO-2248")
    @JiraTestKey("PO-7750")
    void createReportInstance_cannotManuallyCreate_422() throws Exception {
        givenUserStateForBusinessUnits((short)1);

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
    @JiraStory("PO-2252")
    @JiraEpic("PO-2248")
    @JiraTestKey("PO-7743")
    void createReportInstance_wrongBU_403() throws Exception {
        givenUserStateForBusinessUnits((short)1);

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
    @JiraStory("PO-2252")
    @JiraEpic("PO-2248")
    @JiraTestKey("PO-7752")
    void createReportInstance_notAllBUs_403() throws Exception {
        givenUserStateForBusinessUnits((short)1);

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
    @JiraStory("PO-2252")
    @JiraEpic("PO-2248")
    @JiraTestKey("PO-7745")
    void createReportInstance_reportIDNotFound_404() throws Exception {
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
            .andExpect(jsonPath("$.type")
                .value("https://hmcts.gov.uk/problems/entity-not-found"))
            .andExpect(jsonPath("$.title").value("Entity Not Found"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("The requested entity could not be found"))
            .andExpect(jsonPath("$.retriable").value(false));
    }

    @Test
    @JiraStory("PO-2252")
    @JiraEpic("PO-2248")
    @JiraTestKey("PO-7748")
    void createReportInstance_missingBUs_400() throws Exception {
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
    @JiraStory("PO-2252")
    @JiraEpic("PO-2248")
    @JiraTestKey("PO-7749")
    void createReportInstance_malformedRequest_400() throws Exception {
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
            .andExpect(jsonPath("$.type")
                .value("https://hmcts.gov.uk/problems/message-not-readable"))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").value(
                "The request body could not be read. It may be missing or invalid JSON."))
            .andExpect(jsonPath("$.retriable").value(false));
    }

    @Test
    @JiraStory("PO-2252")
    @JiraEpic("PO-2248")
    @JiraTestKey("PO-7747")
    void createReportInstance_publishFail() throws Exception {
        doThrow(new IllegalArgumentException("Unable to publish report queue message"))
            .when(reportQueuePublisher).publish(anyLong());

        givenUserStateForBusinessUnits((short)1);

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
    @JiraStory("PO-2252")
    @JiraEpic("PO-2248")
    @JiraTestKey("PO-7741")
    void createReportInstance_reportParameterValidation_mandatoryFieldsNotSuppliedFail() throws Exception {
        givenUserStateForBusinessUnits((short)1);

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
        log.info(":createReportInstance_reportParameterValidation_mandatoryFieldsNotSuppliedFail response:\n{}",
            ToJsonString.toPrettyJson(body));
        resultActions.andExpect(status().isUnprocessableContent());
    }

    @Test
    @JiraStory("PO-2252")
    @JiraEpic("PO-2248")
    @JiraTestKey("PO-7751")
    void createReportInstance_reportParameterValidation_unknownParameterFail() throws Exception {
        givenUserStateForBusinessUnits((short)1);

        CreateReportInstanceRequestReports request = CreateReportInstanceRequestReports.builder()
            .reportId(REPORT_1BU_ID)
            .reportName(null)
            .businessUnitIds(List.of(1))
            .reportParameters(Map.of(
                "NOT A PARAMETER", "NOT A VALUE!",
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
        log.info(":createReportInstance_reportParameterValidation_unknownParameterFail payload: {}", payload);

        ResultActions resultActions = mockMvc.perform(
            post(URL_BASE).contentType(MediaType.APPLICATION_JSON).content(payload));


        // Assert
        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":createReportInstance_reportParameterValidation_unknownParameterFail response:\n{}",
            ToJsonString.toPrettyJson(body));
        resultActions.andExpect(status().isUnprocessableContent())
            .andExpect(jsonPath("$.retriable").value(true));
    }

    @Test
    @JiraStory("PO-2252")
    @JiraEpic("PO-2248")
    @JiraTestKey("PO-7754")
    void createReportInstance_reportParameterValidation_parameterTypeMismatchFail() throws Exception {
        givenUserStateForBusinessUnits((short)1);

        CreateReportInstanceRequestReports request = CreateReportInstanceRequestReports.builder()
            .reportId(REPORT_1BU_ID)
            .reportName(null)
            .businessUnitIds(List.of(1))
            .reportParameters(Map.of(
                "date-param", "NOT A DATE!",
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
        log.info(":createReportInstance_reportParameterValidation_parameterTypeMismatchFail payload: {}", payload);

        ResultActions resultActions = mockMvc.perform(
            post(URL_BASE).contentType(MediaType.APPLICATION_JSON).content(payload));


        // Assert
        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":createReportInstance_reportParameterValidation_parameterTypeMismatchFail response:\n{}",
            ToJsonString.toPrettyJson(body));
        resultActions.andExpect(status().isUnprocessableContent())
            .andExpect(jsonPath("$.retriable").value(true));
    }

    @Test
    @JiraStory("PO-2252")
    @JiraEpic("PO-2248")
    @JiraTestKey("PO-7753")
    void createReportInstance_repeatSuccess() throws Exception {
        givenUserStateForBusinessUnits((short)1);

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

        ResultActions resultActions1 = mockMvc.perform(
            post(URL_BASE).contentType(MediaType.APPLICATION_JSON).content(payload));
        ResultActions resultActions2 = mockMvc.perform(
            post(URL_BASE).contentType(MediaType.APPLICATION_JSON).content(payload));

        // Assert
        String body1 = resultActions1.andReturn().getResponse().getContentAsString();
        log.info(":createReportInstance_singleBU 1st response:\n{}", ToJsonString.toPrettyJson(body1));
        resultActions1.andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        String body2 = resultActions2.andReturn().getResponse().getContentAsString();
        log.info(":createReportInstance_singleBU 2nd response:\n{}", ToJsonString.toPrettyJson(body2));
        resultActions2.andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        //verify data in db row saved correctly
        CreateReportInstanceResponseReports dto1 = objectMapper
            .readValue(body1, CreateReportInstanceResponseReports.class);
        CreateReportInstanceResponseReports dto2 = objectMapper
            .readValue(body2, CreateReportInstanceResponseReports.class);
        assertNotEquals(dto1.getReportInstanceId(), dto2.getReportInstanceId());
    }

    private void givenUserStateForBusinessUnits(short... businessUnitIds) {
        Set<BusinessUnitUser> businessUnitUsers = new java.util.HashSet<>();
        for (short businessUnitId : businessUnitIds) {
            businessUnitUsers.add(BusinessUnitUser.builder().businessUnitId(businessUnitId).build());
        }

        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(UserState.builder()
            .userId(USER_ID)
            .userName(USER_NAME)
            .businessUnitUser(businessUnitUsers)
            .build());
    }
}
