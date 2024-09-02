package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.search.BusinessUnitSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.ConfigurationItemEntity;
import uk.gov.hmcts.opal.entity.projection.BusinessUnitReferenceData;
import uk.gov.hmcts.opal.service.opal.BusinessUnitService;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = BusinessUnitController.class)
@ActiveProfiles({"integration"})
class BusinessUnitControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    @Qualifier("businessUnitServiceProxy")
    BusinessUnitService businessUnitService;

    @MockBean
    UserStateService userStateService;

    @Test
    void testGetBusinessUnitById() throws Exception {
        BusinessUnitEntity businessUnitEntity = createBusinessUnitEntity();

        when(businessUnitService.getBusinessUnit((short)1)).thenReturn(businessUnitEntity);

        mockMvc.perform(get("/api/business-unit/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.businessUnitId").value(1))
            .andExpect(jsonPath("$.businessUnitName").value("Business Unit 001"))
            .andExpect(jsonPath("$.businessUnitCode").value("AAAA"))
            .andExpect(jsonPath("$.businessUnitType").value("LARGE UNIT"))
            .andExpect(jsonPath("$.accountNumberPrefix").value("XX"))
            .andExpect(jsonPath("$.opalDomain").value("Fines"))
            .andExpect(jsonPath("$.parentBusinessUnit.businessUnitId").value(99));
    }


    @Test
    void testGetBusinessUnitById_WhenBusinessUnitDoesNotExist() throws Exception {
        when(businessUnitService.getBusinessUnit((short)2)).thenReturn(null);

        mockMvc.perform(get("/api/business-unit/2"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testPostBusinessUnitsSearch() throws Exception {
        BusinessUnitEntity businessUnitEntity = createBusinessUnitEntity();

        when(businessUnitService.searchBusinessUnits(any(BusinessUnitSearchDto.class)))
            .thenReturn(singletonList(businessUnitEntity));

        mockMvc.perform(post("/api/business-unit/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"value\"}"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].businessUnitId").value(1))
            .andExpect(jsonPath("$[0].businessUnitName").value("Business Unit 001"))
            .andExpect(jsonPath("$[0].businessUnitCode").value("AAAA"))
            .andExpect(jsonPath("$[0].businessUnitType").value("LARGE UNIT"))
            .andExpect(jsonPath("$[0].accountNumberPrefix").value("XX"))
            .andExpect(jsonPath("$[0].opalDomain").value("Fines"))
            .andExpect(jsonPath("$[0].parentBusinessUnit.businessUnitId").value(99));
    }

    @Test
    void testPostBusinessUnitsSearch_WhenBusinessUnitDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/business-unit/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"2\"}"))
            .andExpect(status().isOk());
    }

    @Test
    void testGetBusinessUnitRefData() throws Exception {
        BusinessUnitReferenceData refData = createBusinessUnitRefData();

        when(businessUnitService.getReferenceData(any())).thenReturn(singletonList(refData));

        mockMvc.perform(get("/api/business-unit/ref-data")
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.refData[0].businessUnitId").value(1))
            .andExpect(jsonPath("$.refData[0].businessUnitName").value("Business Unit 001"))
            .andExpect(jsonPath("$.refData[0].businessUnitCode").value("AAAA"))
            .andExpect(jsonPath("$.refData[0].businessUnitType").value("LARGE UNIT"))
            .andExpect(jsonPath("$.refData[0].accountNumberPrefix").value("XX"))
            .andExpect(jsonPath("$.refData[0].opalDomain").value("Fines"));
    }

    @Test
    void testGetBusinessUnitRefData_Permission_success() throws Exception {
        BusinessUnitReferenceData refData = createBusinessUnitRefData();
        UserState userState = Mockito.mock(UserState.class);

        when(businessUnitService.getReferenceData(any())).thenReturn(singletonList(refData));
        when(userStateService.getUserStateUsingAuthToken(anyString())).thenReturn(userState);
        when(userState.allRolesWithPermission(any())).thenReturn(new TestUserRoles(true));

        mockMvc.perform(get("/api/business-unit/ref-data/?permission=MANUAL_ACCOUNT_CREATION")
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.refData[0].businessUnitId").value(1))
            .andExpect(jsonPath("$.refData[0].businessUnitName").value("Business Unit 001"))
            .andExpect(jsonPath("$.refData[0].businessUnitCode").value("AAAA"))
            .andExpect(jsonPath("$.refData[0].businessUnitType").value("LARGE UNIT"))
            .andExpect(jsonPath("$.refData[0].accountNumberPrefix").value("XX"))
            .andExpect(jsonPath("$.refData[0].opalDomain").value("Fines"));
    }

    @Test
    void testGetBusinessUnitRefData_Permission_empty() throws Exception {
        BusinessUnitReferenceData refData = createBusinessUnitRefData();
        UserState userState = Mockito.mock(UserState.class);

        when(businessUnitService.getReferenceData(any())).thenReturn(singletonList(refData));
        when(userStateService.getUserStateUsingAuthToken(anyString())).thenReturn(userState);
        when(userState.allRolesWithPermission(any())).thenReturn(new TestUserRoles(false));

        mockMvc.perform(get("/api/business-unit/ref-data/?permission=MANUAL_ACCOUNT_CREATION")
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(0));
    }

    private BusinessUnitEntity createBusinessUnitEntity() {
        return BusinessUnitEntity.builder()
            .businessUnitId((short)1)
            .businessUnitName("Business Unit 001")
            .businessUnitCode("AAAA")
            .businessUnitType("LARGE UNIT")
            .accountNumberPrefix("XX")
            .parentBusinessUnit(BusinessUnitEntity.builder().businessUnitId((short)99).build())
            .opalDomain("Fines")
            .welshLanguage(null)
            .configurationItems(List.of(
                ConfigurationItemEntity.builder()
                    .itemName("Config Item Name")
                    .itemValue("Config Item Value")
                    .itemValues(List.of("Config Item Values 1", "Config Item Values 2"))
                    .build()))
            .build();
    }

    private BusinessUnitReferenceData createBusinessUnitRefData() {
        return new BusinessUnitReferenceData(
            (short)1, "Business Unit 001", "AAAA", "LARGE UNIT",
            "XX", "Fines", null, null);
    }

    private class TestUserRoles implements UserState.UserRoles {
        private final boolean contains;

        public TestUserRoles(boolean contains) {
            this.contains = contains;
        }

        @Override
        public boolean containsBusinessUnit(Short businessUnitId) {
            return contains;
        }
    }
}
