package uk.gov.hmcts.opal.controllers.r1a;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.clearInvocations;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.SchemaPaths.GET_PROSECUTORS_REF_DATA_RESPONSE;
import static uk.gov.hmcts.opal.support.SpyInvocationSupport.countInvocationsByMethodName;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.repository.ProsecutorLegacyRepository;
import uk.gov.hmcts.opal.repository.ProsecutorRepository;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

@ActiveProfiles({"integration", "legacy"})
@Sql(scripts = "classpath:db/insertData/insert_into_prosecutors_legacy.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_prosecutors_legacy.sql", executionPhase = AFTER_TEST_CLASS)
@DisplayName("Legacy Prosecutor Controller Integration Test")
@Isolated
@Tag("ExtendedTest")
class LegacyProsecutorControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/prosecutors";

    @MockitoSpyBean
    private ProsecutorRepository prosecutorRepository;

    @MockitoSpyBean
    private ProsecutorLegacyRepository prosecutorLegacyRepository;

    @MockitoSpyBean
    private JsonSchemaValidationService jsonSchemaValidationService;

    @Test
    void getProsecutorByKey_returnsLegacyShape() throws Exception {
        clearRepositoryInvocations();

        mockMvc.perform(get(URL_BASE + "/1111"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.prosecutor_id").value(1111))
            .andExpect(jsonPath("$.prosecutor_code").doesNotExist())
            .andExpect(jsonPath("$.name").value("Legacy AA1 Chief Prosecutor"));

        assertEquals(1, countInvocationsByMethodName(prosecutorLegacyRepository, "findById"));
        assertEquals(0, countInvocationsByMethodName(prosecutorRepository, "findById"));
    }

    @Test
    void getProsecutorsRefData_returnsLegacyShapeAndValidSchema() throws Exception {
        clearRepositoryInvocations();

        ResultActions actions = mockMvc.perform(get(URL_BASE).param("q", "Legacy AA1"));
        String body = actions.andReturn().getResponse().getContentAsString();

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(greaterThan(0)))
            .andExpect(jsonPath("$.ref_data[0].prosecutor_id").isNumber())
            .andExpect(jsonPath("$.ref_data[0].prosecutor_code").doesNotExist());
        jsonSchemaValidationService.validateOrError(body, GET_PROSECUTORS_REF_DATA_RESPONSE);

        assertEquals(1, countInvocationsByMethodName(prosecutorLegacyRepository, "findBy"));
        assertEquals(0, countInvocationsByMethodName(prosecutorRepository, "findBy"));
    }

    private void clearRepositoryInvocations() {
        clearInvocations(prosecutorRepository);
        clearInvocations(prosecutorLegacyRepository);
    }
}
