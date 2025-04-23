package uk.gov.hmcts.opal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.dto.search.MajorCreditorSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.MajorCreditorEntity;
import uk.gov.hmcts.opal.entity.projection.MajorCreditorReferenceData;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.gov.hmcts.opal.service.opal.MajorCreditorService;

import java.time.LocalDateTime;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
@Slf4j(topic = "opal.MajorCreditorControllerIntegrationTest")
@DisplayName("MajorCreditorController Integration Test")
class MajorCreditorControllerIntegrationTest {

    private static final String URL_BASE = "/major-creditors";

    private static final String GET_MAJOR_CREDS_REF_DATA_RESPONSE = "getMajorCredRefDataResponse.json";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    @Qualifier("majorCreditorServiceProxy")
    MajorCreditorService majorCreditorService;

    @SpyBean
    private JsonSchemaValidationService jsonSchemaValidationService;

    @Test
    @DisplayName("Get major creditor by ID [@PO-349, PO-304]")
    void testGetMajorCreditorById() throws Exception {
        MajorCreditorEntity majorCreditorEntity = createMajorCreditorEntity();

        when(majorCreditorService.getMajorCreditor(1L)).thenReturn(majorCreditorEntity);

        mockMvc.perform(get(URL_BASE + "/1"))
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
    @DisplayName("No major creditor returned when major creditor does not exist [@PO-349, PO-304]")
    void testGetMajorCreditorById_WhenMajorCreditorDoesNotExist() throws Exception {
        when(majorCreditorService.getMajorCreditor(2L)).thenReturn(null);

        mockMvc.perform(get(URL_BASE + "/2"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Verify search result for major creditor created by POST request [@PO-349, PO-304]")
    void testPostMajorCreditorsSearch() throws Exception {
        MajorCreditorEntity majorCreditorEntity = createMajorCreditorEntity();

        when(majorCreditorService.searchMajorCreditors(any(MajorCreditorSearchDto.class)))
            .thenReturn(singletonList(majorCreditorEntity));

        mockMvc.perform(post(URL_BASE + "/search")
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
    @DisplayName("Verify no search result when major creditor does not exist [@PO-349, PO-304]")
    void testPostMajorCreditorsSearch_WhenMajorCreditorDoesNotExist() throws Exception {
        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"2\"}"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Endpoint correctly retrieves major creditor reference data [@PO-349, PO-304]")
    void testGetMajorCreditorsRefData() throws Exception {

        MajorCreditorReferenceData refData  = MajorCreditorReferenceData.builder()
            .majorCreditorId(1L)
            .businessUnitId((short)007)
            .majorCreditorCode("MC_001")
            .name("Major Credit Card Ltd")
            .postcode("MN12 4TT")
            .creditorAccountId(99L)
            .accountNumber("AN001-002")
            .creditorAccountType("AT8")
            .prosecutionService(Boolean.TRUE)
            .minorCreditorPartyId(505L)
            .fromSuspense(Boolean.FALSE)
            .holdPayout(Boolean.TRUE)
            .lastChangedDate(LocalDateTime.now())
            .build();

        when(majorCreditorService.getReferenceData(any(), any())).thenReturn(singletonList(refData));

        ResultActions actions = mockMvc.perform(get(URL_BASE).header("authorization", "Bearer some_value"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetMajorCreditorRefData: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.refData[0].major_creditor_id").value(1))
            .andExpect(jsonPath("$.refData[0].major_creditor_code").value("MC_001"))
            .andExpect(jsonPath("$.refData[0].name").value("Major Credit Card Ltd"))
            .andExpect(jsonPath("$.refData[0].postcode").value("MN12 4TT"))
            .andExpect(jsonPath("$.refData[0].creditor_account_id").value(99L))
            .andExpect(jsonPath("$.refData[0].account_number").value("AN001-002"))
            .andExpect(jsonPath("$.refData[0].creditor_account_type").value("AT8"))
            .andExpect(jsonPath("$.refData[0].prosecution_service").value(Boolean.TRUE))
            .andExpect(jsonPath("$.refData[0].minor_creditor_party_id").value(505L))
            .andExpect(jsonPath("$.refData[0].from_suspense").value(Boolean.FALSE))
            .andExpect(jsonPath("$.refData[0].hold_payout").value(Boolean.TRUE))
            .andReturn();

        assertTrue(jsonSchemaValidationService.isValid(body, GET_MAJOR_CREDS_REF_DATA_RESPONSE));
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
