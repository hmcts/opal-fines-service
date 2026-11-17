package uk.gov.hmcts.opal.controllers.r1c;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.PrisonEntity;
import tools.jackson.databind.JsonNode;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles("integration")
@Transactional
@JiraStory("PO-11025")
@JiraEpic("PO-10769")
class PrisonsApiControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL = "/prisons";
    private long prisonId;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void setUpPrison() {
        PrisonEntity prison = PrisonEntity.builder()
            .name("Integration Prison")
            .addressLine1("Line 1")
            .addressLine2("Line 2")
            .addressLine3("Line 3")
            .addressLine4("Line 4")
            .addressLine5("Line 5")
            .postcode("IP1 1AA")
            .build();
        entityManager.persist(prison);
        entityManager.flush();
        prisonId = prison.getPrisonId();
    }

    private void persistPrison(String name) {
        entityManager.persist(PrisonEntity.builder()
            .name(name)
            .addressLine1("Line 1")
            .postcode("IP1 1AA")
            .build());
    }

    private int indexOfName(JsonNode prisons, String name) {
        for (int index = 0; index < prisons.size(); index++) {
            if (name.equals(prisons.get(index).get("name").asText())) {
                return index;
            }
        }
        return -1;
    }

    @Test
    @JiraTestKey("PO-11025")
    void shouldGetPrisonById() throws Exception {
        mockMvc.perform(get(URL + "/" + prisonId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.prison_id").value(prisonId))
            .andExpect(jsonPath("$.name").value("Integration Prison"))
            .andExpect(jsonPath("$.address_line_1").value("Line 1"))
            .andExpect(jsonPath("$.address_line_4").value("Line 4"))
            .andExpect(jsonPath("$.address_line_5").value("Line 5"))
            .andExpect(jsonPath("$.postcode").value("IP1 1AA"));
    }

    @Test
    @JiraTestKey("PO-11025")
    void shouldReturnPrisonReferenceData() throws Exception {
        mockMvc.perform(get(URL))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(greaterThan(0)))
            .andExpect(jsonPath("$.prisons[?(@.prison_id == " + prisonId + ")].name",
                hasItem("Integration Prison")));
    }

    @Test
    @JiraTestKey("PO-11025")
    void shouldReturnNotFoundForUnknownPrison() throws Exception {
        mockMvc.perform(get(URL + "/999999999999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @JiraTestKey("PO-11025")
    void shouldReturnPrisonsInNameOrder() throws Exception {
        entityManager.clear();
        persistPrison("Zulu Integration Prison");
        persistPrison("Alpha Integration Prison");
        entityManager.flush();

        var response = mockMvc.perform(get(URL))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();
        JsonNode prisons = objectMapper.readTree(response.getContentAsString()).get("prisons");

        assertThat(indexOfName(prisons, "Alpha Integration Prison"))
            .isLessThan(indexOfName(prisons, "Zulu Integration Prison"));
    }
}
