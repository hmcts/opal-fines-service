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
import uk.gov.hmcts.opal.dto.search.EnforcerSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.entity.projection.EnforcerReferenceData;
import uk.gov.hmcts.opal.service.opal.EnforcerService;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = EnforcerController.class)
@ActiveProfiles({"integration"})
class EnforcerControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    @Qualifier("enforcerServiceProxy")
    EnforcerService enforcerService;

    @Test
    void testGetEnforcerById() throws Exception {
        EnforcerEntity enforcerEntity = createEnforcerEntity();

        when(enforcerService.getEnforcer(1L)).thenReturn(enforcerEntity);

        mockMvc.perform(get("/api/enforcer/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.enforcerId").value(1))
            .andExpect(jsonPath("$.enforcerCode").value(7))
            .andExpect(jsonPath("$.warrantReferenceSequence").value("WARR-REF-SEQ-666"))
            .andExpect(jsonPath("$.warrantRegisterSequence").value(666))
            .andExpect(jsonPath("$.businessUnit.businessUnitId").value(3))
            .andExpect(jsonPath("$.name").value("Herbert the Enforcer"))
            .andExpect(jsonPath("$.addressLine1").value("Enforcer Road"))
            .andExpect(jsonPath("$.addressLine2").value("Enforcer Town"))
            .andExpect(jsonPath("$.addressLine3").value("Enforcer County"))
            .andExpect(jsonPath("$.postcode").value("EN99 9EN"))
            .andExpect(jsonPath("$.nameCy").value("Herbert the Enforcer CY"))
            .andExpect(jsonPath("$.addressLine1Cy").value("Enforcer Road CY"))
            .andExpect(jsonPath("$.addressLine2Cy").value("Enforcer Town CY"))
            .andExpect(jsonPath("$.addressLine3Cy").value("Enforcer County CY"));
    }


    @Test
    void testGetEnforcerById_WhenEnforcerDoesNotExist() throws Exception {
        when(enforcerService.getEnforcer(2L)).thenReturn(null);

        mockMvc.perform(get("/api/enforcer/2"))
            .andExpect(status().isNoContent());
    }

    @Test
    void testPostEnforcersSearch() throws Exception {
        EnforcerEntity enforcerEntity = createEnforcerEntity();

        when(enforcerService.searchEnforcers(any(EnforcerSearchDto.class))).thenReturn(singletonList(enforcerEntity));

        mockMvc.perform(post("/api/enforcer/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"value\"}"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].enforcerId").value(1))
            .andExpect(jsonPath("$[0].enforcerCode").value(7))
            .andExpect(jsonPath("$[0].warrantReferenceSequence").value("WARR-REF-SEQ-666"))
            .andExpect(jsonPath("$[0].warrantRegisterSequence").value(666))
            .andExpect(jsonPath("$[0].businessUnit.businessUnitId").value(3))
            .andExpect(jsonPath("$[0].name").value("Herbert the Enforcer"))
            .andExpect(jsonPath("$[0].addressLine1").value("Enforcer Road"))
            .andExpect(jsonPath("$[0].addressLine2").value("Enforcer Town"))
            .andExpect(jsonPath("$[0].addressLine3").value("Enforcer County"))
            .andExpect(jsonPath("$[0].postcode").value("EN99 9EN"))
            .andExpect(jsonPath("$[0].nameCy").value("Herbert the Enforcer CY"))
            .andExpect(jsonPath("$[0].addressLine1Cy").value("Enforcer Road CY"))
            .andExpect(jsonPath("$[0].addressLine2Cy").value("Enforcer Town CY"))
            .andExpect(jsonPath("$[0].addressLine3Cy").value("Enforcer County CY"));
    }

    @Test
    void testPostEnforcersSearch_WhenEnforcerDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/enforcer/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"2\"}"))
            .andExpect(status().isNoContent());
    }

    @Test
    void testGetEnforcerRefData() throws Exception {
        EnforcerReferenceData refData = new EnforcerReferenceData(1L, (short)2,
                                                                  "Enforcers UK Ltd", "Enforcers Wales Ltd");

        when(enforcerService.getReferenceData(any())).thenReturn(singletonList(refData));

        mockMvc.perform(get("/api/enforcer/ref-data")
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.refData[0].enforcerId").value(1))
            .andExpect(jsonPath("$.refData[0].enforcerCode").value(2))
            .andExpect(jsonPath("$.refData[0].name").value("Enforcers UK Ltd"))
            .andExpect(jsonPath("$.refData[0].nameCy").value("Enforcers Wales Ltd"));
    }


    private EnforcerEntity createEnforcerEntity() {
        return EnforcerEntity.builder()
            .enforcerId(1L)
            .enforcerCode((short)7)
            .warrantReferenceSequence("WARR-REF-SEQ-666")
            .warrantRegisterSequence(666)
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short)3).build())
            .name("Herbert the Enforcer")
            .addressLine1("Enforcer Road")
            .addressLine2("Enforcer Town")
            .addressLine3("Enforcer County")
            .postcode("EN99 9EN")
            .nameCy("Herbert the Enforcer CY")
            .addressLine1Cy("Enforcer Road CY")
            .addressLine2Cy("Enforcer Town CY")
            .addressLine3Cy("Enforcer County CY")
            .build();
    }
}
