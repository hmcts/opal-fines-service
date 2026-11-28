package uk.gov.hmcts.opal.controllers.shared;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "legacy"})
@DisplayName("Legacy mode reference data source integration tests")
@Sql(scripts = "classpath:db/insertData/insert_opal_only_reference_data.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_opal_only_reference_data.sql", executionPhase = AFTER_TEST_CLASS)
@Isolated
class LegacyReferenceDataSourceIntegrationTest extends ReferenceDataSourceIntegrationTest {

    @Test
    @DisplayName("Local justice area reference data excludes rows held only in the Opal table")
    @JiraStory("PO-11209")
    @JiraEpic("PO-9031")
    @JiraStory("PO-10738")
    void localJusticeAreaReferenceDataExcludesOpalOnlyRows() throws Exception {
        mockMvc.perform(get("/local-justice-areas")
                .param("q", "OPAL_ONLY")
                .header("authorization", userStateStub.getBearerToken()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(0))
            .andExpect(jsonPath("$.refData").isEmpty());
    }

    @Test
    @DisplayName("Result reference data excludes rows held only in the Opal table")
    @JiraStory("PO-11209")
    @JiraEpic("PO-9031")
    void resultReferenceDataExcludesOpalOnlyRows() throws Exception {
        mockMvc.perform(get("/results")
                .param("result_ids", "OPONLY")
                .header("authorization", userStateStub.getBearerToken()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(0))
            .andExpect(jsonPath("$.refData").isEmpty());
    }
}
