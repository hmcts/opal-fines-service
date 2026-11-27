package uk.gov.hmcts.opal.controllers.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.PrisonLegacyEntity;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

abstract class ReferenceDataSourceIntegrationTest extends AbstractIntegrationTest {

    private static final long OPAL_ONLY_PRISON_ID = 59000000000005L;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @DisplayName("Court reference data excludes rows held only in the Opal table")
    @JiraStory("PO-11209")
    @JiraEpic("PO-9031")
    void courtReferenceDataExcludesOpalOnlyRows() throws Exception {
        mockMvc.perform(get("/courts")
                .param("q", "OPAL_ONLY")
                .header("authorization", userStateStub.getBearerToken()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(0))
            .andExpect(jsonPath("$.refData").isEmpty());
    }

    @Test
    @DisplayName("Enforcer reference data excludes rows held only in the Opal table")
    @JiraStory("PO-11209")
    @JiraEpic("PO-9031")
    void enforcerReferenceDataExcludesOpalOnlyRows() throws Exception {
        mockMvc.perform(get("/enforcers")
                .param("q", "OPAL_ONLY")
                .header("authorization", userStateStub.getBearerToken()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(0))
            .andExpect(jsonPath("$.refData").isEmpty());
    }

    @Test
    @DisplayName("Major creditor reference data excludes rows held only in the Opal table")
    @JiraStory("PO-11209")
    @JiraEpic("PO-9031")
    void majorCreditorReferenceDataExcludesOpalOnlyRows() throws Exception {
        mockMvc.perform(get("/major-creditors")
                .param("q", "OPAL_ONLY")
                .header("authorization", userStateStub.getBearerToken()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(0))
            .andExpect(jsonPath("$.refData").isEmpty());
    }

    @Test
    @DisplayName("Offence reference data excludes rows held only in the Opal table")
    @JiraStory("PO-11209")
    @JiraEpic("PO-9031")
    void offenceReferenceDataExcludesOpalOnlyRows() throws Exception {
        mockMvc.perform(get("/offences")
                .param("q", "OPAL_ONLY")
                .header("authorization", userStateStub.getBearerToken()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(0))
            .andExpect(jsonPath("$.refData").isEmpty());
    }

    @Test
    @DisplayName("Prosecutor reference data excludes rows held only in the Opal table")
    @JiraStory("PO-11209")
    @JiraEpic("PO-9031")
    void prosecutorReferenceDataExcludesOpalOnlyRows() throws Exception {
        mockMvc.perform(get("/prosecutors")
                .param("q", "OPAL_ONLY")
                .header("authorization", userStateStub.getBearerToken()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(0))
            .andExpect(jsonPath("$.ref_data").isEmpty());
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

    @Test
    @DisplayName("Prison legacy entity excludes rows held only in the Opal table")
    @JiraStory("PO-11209")
    @JiraEpic("PO-9031")
    void prisonLegacyEntityExcludesOpalOnlyRows() {
        // Prisons have no reference-data endpoint, so verify the entity's table mapping directly.
        entityManager.clear();

        assertThat(entityManager.find(PrisonLegacyEntity.class, OPAL_ONLY_PRISON_ID)).isNull();
    }

}
