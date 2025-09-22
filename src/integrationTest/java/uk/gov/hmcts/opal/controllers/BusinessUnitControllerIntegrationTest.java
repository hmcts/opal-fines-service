package uk.gov.hmcts.opal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.gov.hmcts.opal.service.UserStateService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.BusinessUnitControllerIntegrationTest")
@Sql(scripts = "classpath:db/insertData/insert_into_business_units.sql", executionPhase = BEFORE_TEST_CLASS)
@DisplayName("Business Unit Controller Integration Tests")
class BusinessUnitControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/business-units";
    private static final String GET_BUNITS_REF_DATA_RESPONSE =
        SchemaPaths.REFERENCE_DATA + "/getBusinessUnitsRefDataResponse.json";

    @MockitoBean
    UserStateService userStateService;

    @MockitoSpyBean
    private JsonSchemaValidationService jsonSchemaValidationService;

    @Test
    @DisplayName("Get Business Unit by ID - success")
    void testGetBusinessUnitById_success() throws Exception {
        mockMvc.perform(get(URL_BASE + "/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.businessUnitId").value(1))
            .andExpect(jsonPath("$.businessUnitName").value("AAA Business Unit 001"))
            .andExpect(jsonPath("$.businessUnitCode").value("AAAA"))
            .andExpect(jsonPath("$.businessUnitType").value("LARGE UNIT"))
            .andExpect(jsonPath("$.accountNumberPrefix").value("XX"))
            .andExpect(jsonPath("$.opalDomain").value("Fines"))
            .andExpect(jsonPath("$.welshLanguage").value(true))
            .andExpect(jsonPath("$.parentBusinessUnit.businessUnitId").value(99));
    }


    @Test
    void testGetBusinessUnitById_WhenBusinessUnitDoesNotExist() throws Exception {
        mockMvc.perform(get(URL_BASE + "/2"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testPostBusinessUnitsSearch() throws Exception {
        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"businessUnitId\":\"1\"}"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].businessUnitId").value(1))
            .andExpect(jsonPath("$[0].businessUnitName").value("AAA Business Unit 001"))
            .andExpect(jsonPath("$[0].businessUnitCode").value("AAAA"))
            .andExpect(jsonPath("$[0].businessUnitType").value("LARGE UNIT"))
            .andExpect(jsonPath("$[0].accountNumberPrefix").value("XX"))
            .andExpect(jsonPath("$[0].opalDomain").value("Fines"))
            .andExpect(jsonPath("$[0].parentBusinessUnit.businessUnitId").value(99));
    }

    @Test
    void testPostBusinessUnitsSearch_WhenBusinessUnitDoesNotExist() throws Exception {
        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"businessUnitId\":\"2\"}"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Get Business Unit Ref Data [@PO-304, @PO-313]")
    void testGetBusinessUnitsRefData() throws Exception {
        ResultActions actions =  mockMvc.perform(get(URL_BASE)
                                                     .header("authorization", "Bearer some_value"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetBusinessUnitRefData: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(97))
            .andExpect(jsonPath("$.refData[0].business_unit_id").value(1))
            .andExpect(jsonPath("$.refData[0].business_unit_name").value("AAA Business Unit 001"))
            .andExpect(jsonPath("$.refData[0].business_unit_code").value("AAAA"))
            .andExpect(jsonPath("$.refData[0].business_unit_type").value("LARGE UNIT"))
            .andExpect(jsonPath("$.refData[0].account_number_prefix").value("XX"))
            .andExpect(jsonPath("$.refData[0].welsh_language").value(true))
            .andExpect(jsonPath("$.refData[0].opal_domain").value("Fines"));

        jsonSchemaValidationService.validateOrError(body, GET_BUNITS_REF_DATA_RESPONSE);
    }

    @Test
    void testGetBusinessUnitRefData_Permission_success() throws Exception {
        UserState userState = Mockito.mock(UserState.class);

        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(userState);
        when(userState.allBusinessUnitUsersWithPermission(any())).thenReturn(new TestUserBusinessUnits(true));

        mockMvc.perform(get(URL_BASE + "?permission=CREATE_MANAGE_DRAFT_ACCOUNTS")
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(97))
            .andExpect(jsonPath("$.refData[0].business_unit_id").value(1))
            .andExpect(jsonPath("$.refData[0].business_unit_name").value("AAA Business Unit 001"))
            .andExpect(jsonPath("$.refData[0].business_unit_code").value("AAAA"))
            .andExpect(jsonPath("$.refData[0].business_unit_type").value("LARGE UNIT"))
            .andExpect(jsonPath("$.refData[0].account_number_prefix").value("XX"))
            .andExpect(jsonPath("$.refData[0].opal_domain").value("Fines"));
    }

    @Test
    void testGetBusinessUnitRefData_Permission_empty() throws Exception {
        UserState userState = Mockito.mock(UserState.class);

        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(userState);
        when(userState.allBusinessUnitUsersWithPermission(any())).thenReturn(new TestUserBusinessUnits(false));

        mockMvc.perform(get(URL_BASE + "?permission=CREATE_MANAGE_DRAFT_ACCOUNTS")
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(0));
    }

    private class TestUserBusinessUnits implements UserState.UserBusinessUnits {
        private final boolean contains;

        public TestUserBusinessUnits(boolean contains) {
            this.contains = contains;
        }

        @Override
        public boolean containsBusinessUnit(Short businessUnitId) {
            return contains;
        }
    }
}
