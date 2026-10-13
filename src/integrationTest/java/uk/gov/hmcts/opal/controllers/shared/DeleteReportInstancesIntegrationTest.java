package uk.gov.hmcts.opal.controllers.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.repository.ReportInstanceRepository;
import uk.gov.hmcts.opal.service.blobstore.ReportBlobStore;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.DeleteReportInstancesIntegrationTest")
@DisplayName("Testing Support Report Instances Delete Controller Integration Tests")
@Sql(scripts = {
    "classpath:db/insertData/insert_into_report_instances.sql",
    "classpath:db/insertData/update_report_instance_locations_for_deletion_test.sql"
},
    executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_report_instances.sql",
    executionPhase = AFTER_TEST_METHOD)
public class DeleteReportInstancesIntegrationTest extends AbstractIntegrationTest {

    private static final String URL = "/testing-support/report-instances";
    private static final UUID LOCATION_9001 = UUID.fromString("00000000-0000-0000-0000-000000009001");
    private static final UUID LOCATION_9002 = UUID.fromString("00000000-0000-0000-0000-000000009002");

    @Autowired
    private ReportInstanceRepository reportInstanceRepository;

    @MockitoBean
    private ReportBlobStore reportBlobStore;

    @Test
    @DisplayName("PO-10660 - Deletes report instances and stored content")
    @JiraStory("PO-10660")
    @JiraEpic("PO-2532")
    void shouldDeleteReportInstancesAndStoredContent() throws Exception {

        assertThat(reportInstanceRepository.findAllById(List.of(9001L, 9002L, 9003L))).hasSize(3);

        ResultActions actions = mockMvc.perform(delete(URL)
            .queryParam("ids", "9001", "9001", "9002", "999999"));

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$").doesNotExist());

        assertThat(reportInstanceRepository.findAllById(List.of(9001L, 9002L))).isEmpty();
        assertThat(reportInstanceRepository.findById(9003L)).isPresent();
        verify(reportBlobStore).deleteReport(LOCATION_9001);
        verify(reportBlobStore).deleteReport(LOCATION_9002);
    }

    @Test
    @DisplayName("PO-10660 - Deleting missing report instance IDs succeeds")
    @JiraStory("PO-10660")
    @JiraEpic("PO-2532")
    void shouldSucceedWhenReportInstanceIdsDoNotExist() throws Exception {
        mockMvc.perform(delete(URL)
                .queryParam("ids", "999999"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").doesNotExist());

        verifyNoInteractions(reportBlobStore);
    }
}
