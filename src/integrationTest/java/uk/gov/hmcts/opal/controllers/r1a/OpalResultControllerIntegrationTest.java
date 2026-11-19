package uk.gov.hmcts.opal.controllers.r1a;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.entity.result.ResultType;
import uk.gov.hmcts.opal.repository.OpalResultRepository;

@ActiveProfiles("integration")
@TestPropertySource(properties = "launchdarkly.default-flag-values.is-legacy-mode=false")
@Isolated
class OpalResultControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String RESULT_ID = "OPLIT1";

    @Autowired
    private OpalResultRepository resultRepository;

    @BeforeEach
    void createResult() {
        resultRepository.saveAndFlush(ResultEntity.builder()
            .resultId(RESULT_ID)
            .resultTitle("Opal mode result")
            .resultType(ResultType.RESULT)
            .active(true)
            .imposition(true)
            .impositionCategory("Compensation")
            .enforcement(true)
            .resultParameters("[{\"name\":\"example\"}]")
            .manualEnforcement(true)
            .build());
    }

    @AfterEach
    void deleteResult() {
        resultRepository.deleteById(RESULT_ID);
    }

    @Test
    void getResultById_readsOpalTable() throws Exception {
        mockMvc.perform(get("/results/OPLIT1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result_id").value("OPLIT1"))
            .andExpect(jsonPath("$.result_title").value("Opal mode result"))
            .andExpect(jsonPath("$.imposition_category").value("Compensation"))
            .andExpect(jsonPath("$.result_parameters").value("[{\"name\":\"example\"}]"));
    }

    @Test
    void getResults_readsAndFiltersOpalTable() throws Exception {
        mockMvc.perform(get("/results").param("result_ids", "OPLIT1")
                .param("active", "true").param("manual_enforcement_only", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.refData[0].result_id").value("OPLIT1"))
            .andExpect(jsonPath("$.refData[0].result_title").value("Opal mode result"));
    }
}
