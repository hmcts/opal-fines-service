package uk.gov.hmcts.opal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.EnforcerControllerIntegrationTest")
@Sql(scripts = "classpath:db/insertData/insert_into_enforcers.sql", executionPhase = BEFORE_TEST_CLASS)
@DisplayName("Enforcer Controller Integration Tests")
class EnforcerControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/enforcers";

    private static final String GET_ENFORCERS_REF_DATA_RESPONSE = "getEnforcersRefDataResponse.json";

    @Autowired
    MockMvc mockMvc;

    @SpyBean
    private JsonSchemaValidationService jsonSchemaValidationService;

    @Test
    void testGetEnforcerById() throws Exception {
        ResultActions actions = mockMvc.perform(get(URL_BASE + "/1"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetEnforcerById: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.enforcerId").value(1L))
            .andExpect(jsonPath("$.enforcerCode").value(1))
            .andExpect(jsonPath("$.warrantReferenceSequence").value("101/09/00000"))
            .andExpect(jsonPath("$.warrantRegisterSequence").value(666))
            .andExpect(jsonPath("$.businessUnit.businessUnitId").value(5))
            .andExpect(jsonPath("$.name").value("AAA Enforcers"))
            .andExpect(jsonPath("$.addressLine1").value("9 Enforcement Street"))
            .andExpect(jsonPath("$.addressLine2").value("Enformentville"))
            .andExpect(jsonPath("$.addressLine3").value("Enforcementon"))
            .andExpect(jsonPath("$.postcode").value("EF1 1EF"))
            .andExpect(jsonPath("$.nameCy").value(IsNull.nullValue()))
            .andExpect(jsonPath("$.addressLine1Cy").value(IsNull.nullValue()))
            .andExpect(jsonPath("$.addressLine2Cy").value(IsNull.nullValue()))
            .andExpect(jsonPath("$.addressLine3Cy").value(IsNull.nullValue()));
    }

    @Test
    void testGetEnforcerById_WhenEnforcerDoesNotExist() throws Exception {
        mockMvc.perform(get(URL_BASE + "/2"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testPostEnforcersSearch() throws Exception {
        ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"aa\"}"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostEnforcersSearch: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].enforcerId").value(1L))
            .andExpect(jsonPath("$[0].enforcerCode").value(1))
            .andExpect(jsonPath("$[0].warrantReferenceSequence").value("101/09/00000"))
            .andExpect(jsonPath("$[0].warrantRegisterSequence").value(666))
            .andExpect(jsonPath("$[0].businessUnit.businessUnitId").value(5))
            .andExpect(jsonPath("$[0].name").value("AAA Enforcers"))
            .andExpect(jsonPath("$[0].addressLine1").value("9 Enforcement Street"))
            .andExpect(jsonPath("$[0].addressLine2").value("Enformentville"))
            .andExpect(jsonPath("$[0].addressLine3").value("Enforcementon"))
            .andExpect(jsonPath("$[0].postcode").value("EF1 1EF"))
            .andExpect(jsonPath("$[0].nameCy").value(IsNull.nullValue()))
            .andExpect(jsonPath("$[0].addressLine1Cy").value(IsNull.nullValue()))
            .andExpect(jsonPath("$[0].addressLine2Cy").value(IsNull.nullValue()))
            .andExpect(jsonPath("$[0].addressLine3Cy").value(IsNull.nullValue()));
    }

    @Test
    void testPostEnforcersSearch_WhenEnforcerDoesNotExist() throws Exception {
        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"2\"}"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Get Enforcer Ref Data [@PO-304, @PO-316]")
    void testGetEnforcerRefData() throws Exception {
        ResultActions actions = mockMvc.perform(get(URL_BASE)
                                                    .header("authorization", "Bearer some_value"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetEnforcerRefData: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(84))
            .andExpect(jsonPath("$.refData[1].enforcer_id").value(1L))
            .andExpect(jsonPath("$.refData[1].enforcer_code").value(1))
            .andExpect(jsonPath("$.refData[1].name").value("AAA Enforcers"))
            .andExpect(jsonPath("$.refData[1].name_cy").value(IsNull.nullValue()));

        // Currently no Schema to validate against
        // jsonSchemaValidationService.validateOrError(body, GET_ENFORCERS_REF_DATA_RESPONSE);
    }
}
