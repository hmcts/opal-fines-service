package uk.gov.hmcts.opal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationWithSecurityTest;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.support.UserServiceStub.stubAuthorisedUser;
import static uk.gov.hmcts.opal.support.UserServiceStub.stubUserWithCreateManageDraftAccountsPermission;
import static uk.gov.hmcts.opal.support.UserServiceStub.stubUserWithNoPermissions;

@Slf4j(topic = "opal.BusinessUnitControllerIntegrationTest")
@Sql(scripts = "classpath:db/insertData/insert_into_business_units.sql", executionPhase = BEFORE_TEST_CLASS)
@DisplayName("Business Unit Controller Integration Tests")
class BusinessUnitControllerIntegrationTest extends AbstractIntegrationWithSecurityTest {

    private static final String URL_BASE = "/business-units";
    private static final String GET_BUNITS_REF_DATA_RESPONSE =
        SchemaPaths.REFERENCE_DATA + "/getBusinessUnitsRefDataResponse.json";

    @MockitoSpyBean
    private JsonSchemaValidationService jsonSchemaValidationService;

    @BeforeEach
    void stubUserService() {
        stubAuthorisedUser();
    }

    @Test
    @DisplayName("Get Business Unit by ID - success")
    @JiraStory("PO-304")
    @JiraStory("PO-313")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-5792")
    void testGetBusinessUnitById_success() throws Exception {
        mockMvc.perform(get(URL_BASE + "/1")
                .header("authorization", "Bearer " + validToken))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.businessUnitId").value(1))
            .andExpect(jsonPath("$.businessUnitName").value("AAA Business Unit 001"))
            .andExpect(jsonPath("$.businessUnitCode").value("AAAA"))
            .andExpect(jsonPath("$.businessUnitType").value("Area"))
            .andExpect(jsonPath("$.accountNumberPrefix").value("XX"))
            .andExpect(jsonPath("$.opalDomain").value("Fines"))
            .andExpect(jsonPath("$.welshLanguage").value(true))
            .andExpect(jsonPath("$.parentBusinessUnit.businessUnitId").value(99));
    }


    @Test
    @JiraStory("PO-304")
    @JiraStory("PO-313")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-5797")
    void testGetBusinessUnitById_WhenBusinessUnitDoesNotExist() throws Exception {
        mockMvc.perform(get(URL_BASE + "/2")
                .header("authorization", "Bearer " + validToken))
            .andExpect(status().isNotFound());
    }

    @Test
    @JiraStory("PO-304")
    @JiraStory("PO-313")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-5795")
    void testPostBusinessUnitsSearch() throws Exception {
        mockMvc.perform(post(URL_BASE + "/search")
                .header("authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"businessUnitId\":\"1\"}"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].businessUnitId").value(1))
            .andExpect(jsonPath("$[0].businessUnitName").value("AAA Business Unit 001"))
            .andExpect(jsonPath("$[0].businessUnitCode").value("AAAA"))
            .andExpect(jsonPath("$[0].businessUnitType").value("Area"))
            .andExpect(jsonPath("$[0].accountNumberPrefix").value("XX"))
            .andExpect(jsonPath("$[0].opalDomain").value("Fines"))
            .andExpect(jsonPath("$[0].parentBusinessUnit.businessUnitId").value(99));
    }

    @Test
    @JiraStory("PO-304")
    @JiraStory("PO-313")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-5793")
    void testPostBusinessUnitsSearch_WhenBusinessUnitDoesNotExist() throws Exception {
        mockMvc.perform(post(URL_BASE + "/search")
                .header("authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"businessUnitId\":\"2\"}"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Get Business Unit Ref Data [@PO-304, @PO-313]")
    @JiraStory("PO-304")
    @JiraStory("PO-313")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-5794")
    void testGetBusinessUnitsRefData() throws Exception {
        ResultActions actions = mockMvc.perform(get(URL_BASE)
            .header("authorization", "Bearer " + validToken));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetBusinessUnitRefData: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(97))
            .andExpect(jsonPath("$.refData[?(@.business_unit_id == 1)].business_unit_name")
                .value(hasItem("AAA Business Unit 001")))
            .andExpect(jsonPath("$.refData[?(@.business_unit_id == 1)].business_unit_code").value(hasItem("AAAA")))
            .andExpect(jsonPath("$.refData[?(@.business_unit_id == 1)].business_unit_type").value(hasItem("Area")))
            .andExpect(jsonPath("$.refData[?(@.business_unit_id == 1)].account_number_prefix").value(hasItem("XX")))
            .andExpect(jsonPath("$.refData[?(@.business_unit_id == 1)].welsh_language").value(hasItem(true)))
            .andExpect(jsonPath("$.refData[?(@.business_unit_id == 1)].opal_domain").value(hasItem("Fines")));

        jsonSchemaValidationService.validateOrError(body, GET_BUNITS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Get Business Unit Ref Data filtered by business unit type Area")
    @JiraStory("PO-304")
    @JiraStory("PO-313")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-5790")
    void testGetBusinessUnitsRefData_FilterByArea() throws Exception {
        mockMvc.perform(get(URL_BASE)
                .param("q", "Area")
                .header("authorization", "Bearer " + validToken))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(greaterThan(0)))
            .andExpect(jsonPath("$.refData[*].business_unit_id", hasItem(1)))
            .andExpect(jsonPath("$.refData[*].business_unit_type", everyItem(is("Area"))));
    }

    @Test
    @JiraStory("PO-304")
    @JiraStory("PO-313")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-5796")
    void testGetBusinessUnitRefData_Permission_success() throws Exception {
        stubUserWithCreateManageDraftAccountsPermission(1);

        mockMvc.perform(get(URL_BASE + "?permission=CREATE_MANAGE_DRAFT_ACCOUNTS")
                .header("authorization", "Bearer " + validToken))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.refData[?(@.business_unit_id == 1)].business_unit_name")
                .value(hasItem("AAA Business Unit 001")))
            .andExpect(jsonPath("$.refData[?(@.business_unit_id == 1)].business_unit_code").value(hasItem("AAAA")))
            .andExpect(jsonPath("$.refData[?(@.business_unit_id == 1)].business_unit_type").value(hasItem("Area")))
            .andExpect(jsonPath("$.refData[?(@.business_unit_id == 1)].account_number_prefix").value(hasItem("XX")))
            .andExpect(jsonPath("$.refData[?(@.business_unit_id == 1)].opal_domain").value(hasItem("Fines")));
    }

    @Test
    @JiraStory("PO-304")
    @JiraStory("PO-313")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-5791")
    void testGetBusinessUnitRefData_Permission_empty() throws Exception {
        stubUserWithNoPermissions(1);

        mockMvc.perform(get(URL_BASE + "?permission=CREATE_MANAGE_DRAFT_ACCOUNTS")
                .header("authorization", "Bearer " + validToken))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(0));
    }

}
