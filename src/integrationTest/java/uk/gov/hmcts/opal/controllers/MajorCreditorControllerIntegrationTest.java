package uk.gov.hmcts.opal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.MajorCreditorControllerIntegrationTest")
@Sql(scripts = "classpath:db/insertData/insert_into_creditor_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@DisplayName("MajorCreditorController Integration Test")
class MajorCreditorControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/major-creditors";

    private static final String GET_MAJOR_CREDS_REF_DATA_RESPONSE = "getMajorCredRefDataResponse.json";

    @MockitoSpyBean
    private JsonSchemaValidationService jsonSchemaValidationService;

    @Test
    @DisplayName("Get major creditor by ID [@PO-349, PO-304]")
    void testGetMajorCreditorById() throws Exception {
        ResultActions actions = mockMvc.perform(get(URL_BASE + "/1"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetMajorCreditorById: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.majorCreditorId").value(1))
            .andExpect(jsonPath("$.majorCreditorCode").value("AAAA"))
            .andExpect(jsonPath("$.name").value("AAAA Credit Services"))
            .andExpect(jsonPath("$.addressLine1").value("Credit Lane"))
            .andExpect(jsonPath("$.addressLine2").value("Creditville"))
            .andExpect(jsonPath("$.addressLine3").value("Crediton"))
            .andExpect(jsonPath("$.postcode").value("CR1 1CR"));
    }


    @Test
    @DisplayName("No major creditor returned when major creditor does not exist [@PO-349, PO-304]")
    void testGetMajorCreditorById_WhenMajorCreditorDoesNotExist() throws Exception {

        mockMvc.perform(get(URL_BASE + "/2"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Verify search result for major creditor created by POST request [@PO-349, PO-304]")
    void testPostMajorCreditorsSearch() throws Exception {
        ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"businessUnitId\":\"78\"}"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostMajorCreditorsSearch: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].majorCreditorId").value(1))
            .andExpect(jsonPath("$[0].majorCreditorCode").value("AAAA"))
            .andExpect(jsonPath("$[0].name").value("AAAA Credit Services"))
            .andExpect(jsonPath("$[0].addressLine1").value("Credit Lane"))
            .andExpect(jsonPath("$[0].addressLine2").value("Creditville"))
            .andExpect(jsonPath("$[0].addressLine3").value("Crediton"))
            .andExpect(jsonPath("$[0].postcode").value("CR1 1CR"));
    }

    @Test
    @DisplayName("Verify no search result when major creditor does not exist [@PO-349, PO-304]")
    void testPostMajorCreditorsSearch_WhenMajorCreditorDoesNotExist() throws Exception {
        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"majorCreditorId\":\"2\"}"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Endpoint correctly retrieves major creditor reference data [@PO-349, PO-304]")
    void testGetMajorCreditorsRefData() throws Exception {

        ResultActions actions = mockMvc.perform(get(URL_BASE).header("authorization", "Bearer some_value"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetMajorCreditorRefData: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(97))
            .andExpect(jsonPath("$.refData[1].major_creditor_id").value(1))
            .andExpect(jsonPath("$.refData[1].major_creditor_code").value("AAAA"))
            .andExpect(jsonPath("$.refData[1].name").value("AAAA Credit Services"))
            .andExpect(jsonPath("$.refData[1].postcode").value("CR1 1CR"))
            .andExpect(jsonPath("$.refData[1].business_unit_id").value(78))
            .andReturn();

        jsonSchemaValidationService.validateOrError(body, GET_MAJOR_CREDS_REF_DATA_RESPONSE);
    }

}
