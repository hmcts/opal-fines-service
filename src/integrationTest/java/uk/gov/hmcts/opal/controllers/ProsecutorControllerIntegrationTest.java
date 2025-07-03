package uk.gov.hmcts.opal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.SchemaPaths.GET_PROSECUTORS_REF_DATA_RESPONSE;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.ProsecutorControllerIntegrationTest")
@Sql(scripts = "classpath:db/insertData/insert_into_prosecutors.sql", executionPhase = BEFORE_TEST_CLASS)
class ProsecutorControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/prosecutors";

    @MockitoSpyBean
    private JsonSchemaValidationService jsonSchemaValidationService;

    @Test
    void testGetEnforcerById() throws Exception {
        ResultActions actions = mockMvc.perform(get(URL_BASE + "/1"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetEnforcerById: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.prosecutor_id").value(1L))
            .andExpect(jsonPath("$.prosecutor_code").value("AA01"))
            .andExpect(jsonPath("$.name").value("AA1 Chief Prosecutor"))
            .andExpect(jsonPath("$.address_line_1").value("9 Prosecutor Street"))
            .andExpect(jsonPath("$.address_line_2").value("Prosecutorville"))
            .andExpect(jsonPath("$.address_line_3").value("Prosecutorton"))
            .andExpect(jsonPath("$.postcode").value("PR01 2PR"));
    }

    @Test
    void testGetEnforcerById_WhenEnforcerDoesNotExist() throws Exception {
        mockMvc.perform(get(URL_BASE + "/4"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testGetEnforcerRefData() throws Exception {
        ResultActions actions = mockMvc.perform(get(URL_BASE)
                                                    .header("authorization", "Bearer some_value"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetEnforcerRefData: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(3))
            .andExpect(jsonPath("$.ref_data[0].prosecutor_id").value(1L))
            .andExpect(jsonPath("$.ref_data[0].prosecutor_code").value("AA01"))
            .andExpect(jsonPath("$.ref_data[0].name").value("AA1 Chief Prosecutor"))
            .andExpect(jsonPath("$.ref_data[0].address_line_1").value("9 Prosecutor Street"))
            .andExpect(jsonPath("$.ref_data[0].postcode").value("PR01 2PR"))
            .andExpect(jsonPath("$.ref_data[1].prosecutor_id").value(2L))
            .andExpect(jsonPath("$.ref_data[1].prosecutor_code").value("AA02"))
            .andExpect(jsonPath("$.ref_data[1].name").value("AA2 Assistant Prosecutor"))
            .andExpect(jsonPath("$.ref_data[1].address_line_1").value("49 Prosecutor Street"))
            .andExpect(jsonPath("$.ref_data[1].postcode").value("PR01 4PR"))
            .andExpect(jsonPath("$.ref_data[2].prosecutor_id").value(3L))
            .andExpect(jsonPath("$.ref_data[2].prosecutor_code").value("AA03"))
            .andExpect(jsonPath("$.ref_data[2].name").value("AA3 Junior Prosecutor"))
            .andExpect(jsonPath("$.ref_data[2].address_line_1").value("99 Prosecutor Street"))
            .andExpect(jsonPath("$.ref_data[2].postcode").value("PR01 9PR"));

        jsonSchemaValidationService.validateOrError(body, GET_PROSECUTORS_REF_DATA_RESPONSE);
    }
}
