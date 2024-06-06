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
import uk.gov.hmcts.opal.dto.search.MajorCreditorSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.MajorCreditorEntity;
import uk.gov.hmcts.opal.entity.projection.MajorCreditorReferenceData;
import uk.gov.hmcts.opal.service.opal.MajorCreditorService;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = MajorCreditorController.class)
@ActiveProfiles({"integration"})
class MajorCreditorControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    @Qualifier("majorCreditorService")
    MajorCreditorService majorCreditorService;

    @Test
    void testGetMajorCreditorById() throws Exception {
        MajorCreditorEntity majorCreditorEntity = createMajorCreditorEntity();

        when(majorCreditorService.getMajorCreditor(1L)).thenReturn(majorCreditorEntity);

        mockMvc.perform(get("/api/major-creditor/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.majorCreditorId").value(1))
            .andExpect(jsonPath("$.majorCreditorCode").value("CODE"))
            .andExpect(jsonPath("$.name").value("Major Creditor Corp"))
            .andExpect(jsonPath("$.addressLine1").value("Major Creditor Avenue"))
            .andExpect(jsonPath("$.addressLine2").value("Major Creditor City"))
            .andExpect(jsonPath("$.addressLine3").value("Major Creditor County"))
            .andExpect(jsonPath("$.postcode").value("MC99 9MC"));
    }


    @Test
    void testGetMajorCreditorById_WhenMajorCreditorDoesNotExist() throws Exception {
        when(majorCreditorService.getMajorCreditor(2L)).thenReturn(null);

        mockMvc.perform(get("/api/major-creditor/2"))
            .andExpect(status().isNoContent());
    }

    @Test
    void testPostMajorCreditorsSearch() throws Exception {
        MajorCreditorEntity majorCreditorEntity = createMajorCreditorEntity();

        when(majorCreditorService.searchMajorCreditors(any(MajorCreditorSearchDto.class)))
            .thenReturn(singletonList(majorCreditorEntity));

        mockMvc.perform(post("/api/major-creditor/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"value\"}"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].majorCreditorId").value(1))
            .andExpect(jsonPath("$[0].majorCreditorCode").value("CODE"))
            .andExpect(jsonPath("$[0].name").value("Major Creditor Corp"))
            .andExpect(jsonPath("$[0].addressLine1").value("Major Creditor Avenue"))
            .andExpect(jsonPath("$[0].addressLine2").value("Major Creditor City"))
            .andExpect(jsonPath("$[0].addressLine3").value("Major Creditor County"))
            .andExpect(jsonPath("$[0].postcode").value("MC99 9MC"));
    }

    @Test
    void testPostMajorCreditorsSearch_WhenMajorCreditorDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/major-creditor/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"2\"}"))
            .andExpect(status().isNoContent());
    }

    @Test
    void testGetMajorCreditorRefData() throws Exception {
        MajorCreditorReferenceData refData = new MajorCreditorReferenceData(1L, (short)007, "MC_001",
                                                                            "Major Credit Card Ltd", "MN12 4TT");

        when(majorCreditorService.getReferenceData(any(), any())).thenReturn(singletonList(refData));

        mockMvc.perform(get("/api/major-creditor/ref-data")
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.refData[0].majorCreditorId").value(1))
            .andExpect(jsonPath("$.refData[0].majorCreditorCode").value("MC_001"))
            .andExpect(jsonPath("$.refData[0].name").value("Major Credit Card Ltd"))
            .andExpect(jsonPath("$.refData[0].postcode").value("MN12 4TT"));
    }

    private MajorCreditorEntity createMajorCreditorEntity() {
        return MajorCreditorEntity.builder()
            .majorCreditorId(1L)
            .businessUnit(BusinessUnitEntity.builder().build())
            .majorCreditorCode("CODE")
            .name("Major Creditor Corp")
            .addressLine1("Major Creditor Avenue")
            .addressLine2("Major Creditor City")
            .addressLine3("Major Creditor County")
            .postcode("MC99 9MC")
            .build();
    }
}
