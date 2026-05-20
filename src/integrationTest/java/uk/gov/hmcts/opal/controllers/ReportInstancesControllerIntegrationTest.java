package uk.gov.hmcts.opal.controllers;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.List;
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
import uk.gov.hmcts.opal.service.messaging.ReportQueuePublisher;
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
                .reportParameters(new HashMap<>())
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

        //verify data in db saved correctly
        CreateReportInstanceResponseReports dto = objectMapper.readValue(body, CreateReportInstanceResponseReports.class);
        List<ReportInstanceEntity> reportInstanceEntities = reportInstanceRepository.findAll(); //todo findById()

        assertEquals(1, reportInstanceEntities.size());

        //Mockito.verify(reportQueuePublisher).publish(dto.getReportInstanceId().longValue());
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
            .businessUnitIds(List.of(1,2))
            .reportParameters(new HashMap<>())
            .build();

        String payload = objectMapper.writeValueAsString(request);
        log.info(":createReportInstance_wrongBU payload: {}", payload);

        ResultActions resultActions = mockMvc.perform(
            post(URL_BASE).contentType(MediaType.APPLICATION_JSON).content(payload));
        resultActions.andExpect(status().isForbidden());
    }

    public void createReportInstance_reportIDNotFound_404() {

    }

    public void createReportInstance_missingBUs_400() {

    }

    public void createReportInstance_malformedRequest() {

    }
}
