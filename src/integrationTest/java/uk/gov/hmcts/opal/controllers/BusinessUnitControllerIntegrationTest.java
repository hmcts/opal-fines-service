package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.opal.controllers.BusinessUnitController;
import uk.gov.hmcts.opal.dto.search.BusinessUnitSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.service.opal.BusinessUnitService;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
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
    @Qualifier("businessUnitService")
    BusinessUnitService businessUnitService;

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
            .andExpect(jsonPath("$.parentBusinessUnitId").value(99));
    }


    @Test
    void testGetBusinessUnitById_WhenBusinessUnitDoesNotExist() throws Exception {
        when(businessUnitService.getBusinessUnit((short)2)).thenReturn(null);

        mockMvc.perform(get("/api/business-unit/2"))
            .andExpect(status().isNoContent());
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
            .andExpect(jsonPath("$[0].parentBusinessUnitId").value(99));
    }

    @Test
    void testPostBusinessUnitsSearch_WhenBusinessUnitDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/business-unit/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"2\"}"))
            .andExpect(status().isNoContent());
    }

    private BusinessUnitEntity createBusinessUnitEntity() {
        return BusinessUnitEntity.builder()
            .businessUnitId((short)1)
            .businessUnitName("Business Unit 001")
            .businessUnitCode("AAAA")
            .businessUnitType("LARGE UNIT")
            .accountNumberPrefix("XX")
            .parentBusinessUnitId((short)99)
            .build();
    }
}
