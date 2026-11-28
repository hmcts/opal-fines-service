package uk.gov.hmcts.opal.controllers.r1a;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.result.ResultLegacyEntity;
import uk.gov.hmcts.opal.entity.result.ResultOpalEntity;
import uk.gov.hmcts.opal.entity.result.ResultType;
import uk.gov.hmcts.opal.repository.ResultLegacyRepository;
import uk.gov.hmcts.opal.repository.ResultOpalRepository;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;

@ActiveProfiles("integration")
@Isolated
class ResultModeCacheIntegrationTest extends AbstractIntegrationTest {

    private static final String RESULT_ID = "MODE01";

    @MockitoBean
    private DynamicConfigService dynamicConfigService;

    @Autowired
    private ResultLegacyRepository legacyRepository;

    @Autowired
    private ResultOpalRepository opalRepository;

    @BeforeEach
    void createResults() {
        legacyRepository.saveAndFlush(ResultLegacyEntity.builder()
            .resultId(RESULT_ID).resultTitle("Legacy title").resultTitleCy("Legacy title")
            .resultType(ResultType.RESULT).impositionAccruing(false).build());
        opalRepository.saveAndFlush(ResultOpalEntity.builder()
            .resultId(RESULT_ID).resultTitle("Opal title").resultType(ResultType.RESULT).build());
    }

    @AfterEach
    void deleteResults() {
        legacyRepository.deleteById(RESULT_ID);
        opalRepository.deleteById(RESULT_ID);
        clearCaches();
    }

    @Test
    @JiraStory("PO-11027")
    @JiraEpic("PO-10769")
    void resultsAndCachedEntriesAreIsolatedByMode() throws Exception {
        when(dynamicConfigService.isLegacyMode()).thenReturn(true);
        assertSingleResult("Legacy title");
        assertResultsList("Legacy title");

        when(dynamicConfigService.isLegacyMode()).thenReturn(false);
        assertSingleResult("Opal title");
        assertResultsList("Opal title");

        ResultLegacyEntity legacyResult = legacyRepository.findById(RESULT_ID).orElseThrow();
        legacyResult.setResultTitle("Changed legacy");
        legacyRepository.saveAndFlush(legacyResult);
        ResultOpalEntity opalResult = opalRepository.findById(RESULT_ID).orElseThrow();
        opalResult.setResultTitle("Changed opal");
        opalRepository.saveAndFlush(opalResult);
        assertSingleResult("Opal title");
        when(dynamicConfigService.isLegacyMode()).thenReturn(true);
        assertSingleResult("Legacy title");
    }

    private void assertSingleResult(String title) throws Exception {
        mockMvc.perform(get("/results/{id}", RESULT_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result_title").value(title));
    }

    private void assertResultsList(String title) throws Exception {
        mockMvc.perform(get("/results").param("result_ids", RESULT_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.refData[0].result_title").value(title));
    }
}
