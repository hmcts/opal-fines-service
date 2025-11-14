package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.SchemaPaths.GET_PROSECUTORS_REF_DATA_RESPONSE;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.ToJsonString;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.ProsecutorControllerIntegrationTest")
@Sql(scripts = "classpath:db/insertData/insert_into_prosecutors.sql", executionPhase = BEFORE_TEST_CLASS)
@DisplayName("Prosecutor Controller Integration Test")
class ProsecutorControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/prosecutors";

    @Test
    @DisplayName("Get Prosecutor By ID [@PO-1787]")
    void testGetProsecutorById() throws Exception {
        ResultActions actions = mockMvc.perform(get(URL_BASE + "/1111"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetEnforcerById: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.prosecutor_id").value(1111L))
            .andExpect(jsonPath("$.prosecutor_code").value("AA01"))
            .andExpect(jsonPath("$.name").value("AA1 Chief Prosecutor"))
            .andExpect(jsonPath("$.address_line_1").value("9 Prosecutor Street"))
            .andExpect(jsonPath("$.address_line_2").value("Prosecutorville"))
            .andExpect(jsonPath("$.address_line_3").value("Prosecutorton"))
            .andExpect(jsonPath("$.postcode").value("PR01 2PR"));
    }

    @Test
    @DisplayName("Get Prosecutor By ID - Prosecutor Does Not Exist [@PO-1787]")
    void testGetProsecutorById_WhenProsecutorDoesNotExist() throws Exception {
        mockMvc.perform(get(URL_BASE + "/4444"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get Prosecutors as Reference Data [@PO-1787]")
    void testGetProsecutorsRefData() throws Exception {
        ResultActions actions = mockMvc.perform(get(URL_BASE)
                                                    .header("authorization", "Bearer some_value"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetEnforcerRefData: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(48));

        jsonSchemaValidationService.validateOrError(body, GET_PROSECUTORS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Get Prosecutor By ID returns full 60-char address_line_1 [@PO-1787]")
    void testGetProsecutorById_WithSixtyCharAddressLine1() throws Exception {
        ResultActions actions = mockMvc.perform(get(URL_BASE + "/9990")); // 009990 == 9990
        String expected = "123456789012345678901234567890123456789012345678901234567890";

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.prosecutor_id").value(9990L))
            .andExpect(jsonPath("$.prosecutor_code").value("AA04"))
            .andExpect(jsonPath("$.name").value("AA4 Boundary Prosecutor"))
            .andExpect(jsonPath("$.address_line_1").value(expected))
            .andExpect(jsonPath("$.address_line_2").value("Boundaryville"))
            .andExpect(jsonPath("$.address_line_3").value("Boundaryton"));
    }

}
