package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.clearInvocations;
import static uk.gov.hmcts.opal.support.SpyInvocationSupport.countInvocationsByMethodName;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.repository.MajorCreditorRepository;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.MajorCreditorControllerIntegrationTest")
@Sql(scripts = "classpath:db/insertData/insert_into_creditor_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@DisplayName("MajorCreditorController Integration Test")
class MajorCreditorControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/major-creditors";

    private static final String GET_MAJOR_CREDS_REF_DATA_RESPONSE =
        SchemaPaths.REFERENCE_DATA + "/getMajorCredRefDataResponse.json";

    @MockitoSpyBean
    private JsonSchemaValidationService jsonSchemaValidationService;

    @MockitoSpyBean
    private MajorCreditorRepository majorCreditorRepository;

    @Test
    @DisplayName("Get major creditor by ID [@PO-349, PO-304]")
    @JiraStory("PO-349")
    @JiraStory("PO-304")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-5975")
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
    @JiraStory("PO-349")
    @JiraStory("PO-304")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-5973")
    void testGetMajorCreditorById_WhenMajorCreditorDoesNotExist() throws Exception {

        mockMvc.perform(get(URL_BASE + "/2"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Verify search result for major creditor created by POST request [@PO-349, PO-304]")
    @JiraStory("PO-349")
    @JiraStory("PO-304")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-5974")
    void testPostMajorCreditorsSearch() throws Exception {
        ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"businessUnitId\":\"78\"}"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostMajorCreditorsSearch: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[?(@.majorCreditorId == 1)].majorCreditorCode").value(hasItem("AAAA")))
            .andExpect(jsonPath("$[?(@.majorCreditorId == 1)].name").value(hasItem("AAAA Credit Services")))
            .andExpect(jsonPath("$[?(@.majorCreditorId == 1)].addressLine1").value(hasItem("Credit Lane")))
            .andExpect(jsonPath("$[?(@.majorCreditorId == 1)].addressLine2").value(hasItem("Creditville")))
            .andExpect(jsonPath("$[?(@.majorCreditorId == 1)].addressLine3").value(hasItem("Crediton")))
            .andExpect(jsonPath("$[?(@.majorCreditorId == 1)].postcode").value(hasItem("CR1 1CR")));
    }

    @Test
    @DisplayName("Verify no search result when major creditor does not exist [@PO-349, PO-304]")
    @JiraStory("PO-349")
    @JiraStory("PO-304")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-5976")
    void testPostMajorCreditorsSearch_WhenMajorCreditorDoesNotExist() throws Exception {
        mockMvc.perform(post(URL_BASE + "/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"majorCreditorId\":\"2\"}"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Endpoint correctly retrieves major creditor reference data [@PO-349, PO-304]")
    @JiraStory("PO-349")
    @JiraStory("PO-304")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-5972")
    void testGetMajorCreditorsRefData() throws Exception {

        ResultActions actions = mockMvc.perform(get(URL_BASE)
            .header("authorization", userStateStub.getBearerToken()));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetMajorCreditorRefData: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(144))
            .andExpect(jsonPath("$.refData[?(@.major_creditor_id == 1)].major_creditor_code").value(hasItem("AAAA")))
            .andExpect(jsonPath("$.refData[?(@.major_creditor_id == 1)].name")
                .value(hasItem("AAAA Credit Services")))
            .andExpect(jsonPath("$.refData[?(@.major_creditor_id == 1)].postcode").value(hasItem("CR1 1CR")))
            .andExpect(jsonPath("$.refData[?(@.major_creditor_id == 1)].business_unit_id").value(hasItem(78)))
            .andReturn();

        jsonSchemaValidationService.validateOrError(body, GET_MAJOR_CREDS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Major creditor reference data uses cache on repeated identical request")
    @JiraStory("PO-7248")
    @JiraEpic("PO-8248")
    void testGetMajorCreditorsRefData_usesCacheOnRepeatedRequest() throws Exception {
        clearInvocations(majorCreditorRepository);

        String firstBody = performRequest();
        String secondBody = performRequest();

        assertEquals(firstBody, secondBody);
        assertEquals(1, countInvocationsByMethodName(majorCreditorRepository, "findBy"));
    }

    private String performRequest() throws Exception {
        return mockMvc.perform(get(URL_BASE)
                .header("authorization", userStateStub.getBearerToken()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    }

}
