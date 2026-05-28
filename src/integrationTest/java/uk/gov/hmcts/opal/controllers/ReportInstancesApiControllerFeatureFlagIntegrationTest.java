package uk.gov.hmcts.opal.controllers;

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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceRequestReports;
import uk.gov.hmcts.opal.service.UserStateService;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.ReportInstanceControllerIntegrationTest")
@Sql(scripts = "classpath:db/insertData/insert_into_reports.sql", executionPhase = BEFORE_TEST_CLASS)
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1c-enforcement-operational-reporting=false"
})
public class ReportInstancesApiControllerFeatureFlagIntegrationTest extends AbstractIntegrationTest {
    private static final String URL_BASE = "/report-instances";

    @MockitoBean
    UserStateService userStateService;

    @MockitoBean
    private UserState userState;

    @MockitoBean
    private BusinessUnitUser businessUnitUser1;

    @Test
    void createReportInstance_failMethodNotAllowed() throws Exception {
        Mockito.when(userStateService.checkForAuthorisedUser(Mockito.anyString())).thenReturn(userState);
        Mockito.when(userState.getBusinessUnitUser()).thenReturn(Set.of(businessUnitUser1));
        Mockito.when(userState.getUserId()).thenReturn(123L);
        Mockito.when(userState.getUserName()).thenReturn("USER_NAME");
        Mockito.when(businessUnitUser1.getBusinessUnitId()).thenReturn((short)1);

        CreateReportInstanceRequestReports request = CreateReportInstanceRequestReports.builder()
            .reportId("IT-report-2")
            .reportName(null)
            .businessUnitIds(List.of(1))
            .reportParameters(new HashMap<>())
            .build();

        String payload = objectMapper.writeValueAsString(request);
        log.info(":createReportInstance_failMethodNotAllowed payload: {}", payload);

        ResultActions resultActions = mockMvc.perform(
            post(URL_BASE).contentType(MediaType.APPLICATION_JSON).content(payload));

        // Assert
        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":createReportInstance_failMethodNotAllowed response:\n{}", ToJsonString.toPrettyJson(body));
        resultActions.andExpect(status().isMethodNotAllowed())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
            .andExpect(jsonPath("$.title").value("Feature Disabled"))
            .andExpect(jsonPath("$.detail").value("The requested feature is not currently available"));
    }
}
