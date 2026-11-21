package uk.gov.hmcts.opal.controllers.r1a;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.result.ImpositionCategory;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.entity.result.ResultType;
import uk.gov.hmcts.opal.repository.ResultRepository;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles("integration")
@TestPropertySource(properties = "launchdarkly.default-flag-values.is-legacy-mode=false")
@Isolated
class OpalResultControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String RESULT_ID = "OPLIT1";

    @Autowired
    private ResultRepository resultRepository;

    @BeforeEach
    void createResult() {
        resultRepository.saveAndFlush(ResultEntity.builder()
            .resultId(RESULT_ID)
            .resultTitle("Opal mode result")
            .resultType(ResultType.RESULT)
            .active(true)
            .imposition(true)
            .impositionCategory(ImpositionCategory.COMPENSATION)
            .enforcement(true)
            .enforcementOverride(true)
            .generatesHearing(true)
            .resultParameters("[{\"name\":\"example\"}]")
            .manualEnforcement(true)
            .build());
        resultRepository.saveAllAndFlush(List.of(
            ResultEntity.builder().resultId("OPLIT2").resultTitle("Inactive result")
                .resultType(ResultType.RESULT).manualEnforcement(false).build(),
            ResultEntity.builder().resultId("OPLIT3").resultTitle("Mixed result")
                .resultType(ResultType.RESULT).active(true).manualEnforcement(false)
                .generatesHearing(true).enforcementOverride(true).build()
        ));
    }

    @AfterEach
    void deleteResult() {
        resultRepository.deleteAllById(List.of(RESULT_ID, "OPLIT2", "OPLIT3"));
    }

    @Test
    @JiraStory("PO-11027")
    @JiraEpic("PO-10769")
    void getResultById_readsOpalTable() throws Exception {
        mockMvc.perform(get("/results/OPLIT1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result_id").value("OPLIT1"))
            .andExpect(jsonPath("$.result_title").value("Opal mode result"))
            .andExpect(jsonPath("$.imposition_category").value("Compensation"))
            .andExpect(jsonPath("$.result_parameters").value("[{\"name\":\"example\"}]"));
    }

    @Test
    @JiraStory("PO-11027")
    @JiraEpic("PO-10769")
    void getResultById_returnsNotFoundForUnknownId() throws Exception {
        // Given
        String unknownId = "NOPE99";

        // When / Then
        mockMvc.perform(get("/results/{id}", unknownId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.title").value("Entity Not Found"))
            .andExpect(jsonPath("$.detail").value("The requested entity could not be found"));
    }

    @Test
    @JiraStory("PO-11027")
    @JiraEpic("PO-10769")
    void getResults_readsAndFiltersOpalTable() throws Exception {
        mockMvc.perform(get("/results").param("result_ids", "OPLIT1")
                .param("active", "true").param("manual_enforcement_only", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.refData[0].result_id").value("OPLIT1"))
            .andExpect(jsonPath("$.refData[0].result_title").value("Opal mode result"));
    }

    @Test
    @JiraStory("PO-11027")
    @JiraEpic("PO-10769")
    void getResults_returnsKnownIdsInAscendingOrder() throws Exception {
        // Given
        String ids = "OPLIT3,NOPE99,OPLIT1";

        // When / Then
        mockMvc.perform(get("/results").param("result_ids", ids))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.refData[*].result_id", contains("OPLIT1", "OPLIT3")));
    }

    @Test
    @JiraStory("PO-11027")
    @JiraEpic("PO-10769")
    void getResults_returnsEmptyListForUnknownIds() throws Exception {
        // Given
        String ids = "NOPE98,NOPE99";

        // When / Then
        mockMvc.perform(get("/results").param("result_ids", ids))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(0))
            .andExpect(jsonPath("$.refData").isEmpty());
    }

    @ParameterizedTest(name = "{0}={1}")
    @MethodSource("booleanFilterCases")
    @JiraStory("PO-11027")
    @JiraEpic("PO-10769")
    void getResults_appliesBooleanFilters(String filter, String value, List<String> expectedIds) throws Exception {
        // Given
        String ids = "OPLIT1,OPLIT2,OPLIT3";

        // When / Then
        mockMvc.perform(get("/results").param("result_ids", ids).param(filter, value))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(expectedIds.size()))
            .andExpect(jsonPath("$.refData[*].result_id", contains(expectedIds.toArray())));
    }

    private static Stream<Arguments> booleanFilterCases() {
        return Stream.of(
            Arguments.of("active", "true", List.of("OPLIT1", "OPLIT3")),
            Arguments.of("active", "false", List.of("OPLIT2")),
            Arguments.of("manual_enforcement_only", "true", List.of("OPLIT1")),
            Arguments.of("manual_enforcement_only", "false", List.of("OPLIT2", "OPLIT3")),
            Arguments.of("generates_hearing", "true", List.of("OPLIT1", "OPLIT3")),
            Arguments.of("generates_hearing", "false", List.of("OPLIT2")),
            Arguments.of("enforcement", "true", List.of("OPLIT1")),
            Arguments.of("enforcement", "false", List.of("OPLIT2", "OPLIT3")),
            Arguments.of("enforcement_override", "true", List.of("OPLIT1", "OPLIT3")),
            Arguments.of("enforcement_override", "false", List.of("OPLIT2"))
        );
    }

    @Test
    @JiraStory("PO-11027")
    @JiraEpic("PO-10769")
    void getResults_combinesBooleanFilters() throws Exception {
        // Given
        String ids = "OPLIT1,OPLIT2,OPLIT3";

        // When / Then
        mockMvc.perform(get("/results").param("result_ids", ids).param("active", "true")
                .param("manual_enforcement_only", "false"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.refData[0].result_id").value("OPLIT3"));
    }

}
