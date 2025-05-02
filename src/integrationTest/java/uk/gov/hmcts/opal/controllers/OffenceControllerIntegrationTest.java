package uk.gov.hmcts.opal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.OffenceEntity;
import uk.gov.hmcts.opal.entity.projection.OffenceReferenceData;
import uk.gov.hmcts.opal.entity.projection.OffenceSearchData;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

import java.time.LocalDateTime;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j(topic = "opal.OffenceControllerIntegrationTest")
@DisplayName("OffenceController Integration Test")

class OffenceControllerIntegrationTest extends AbstractIntegrationTest {
    @SpyBean
    private JsonSchemaValidationService jsonSchemaValidationService;
    private static final String GET_OFFENCES_REF_DATA_RESPONSE = "getOffencesRefDataResponse.json";
    private static final String POST_OFFENCES_SEARCH_RESPONSE = "postOffencesSearchResponse.json";
    private static final String URL_BASE = "/offences";


    @Test
    @DisplayName("Get offence by ID [@PO-420, PO-272]")
    void testGetOffenceById() throws Exception {
        mockMvc.perform(get(URL_BASE + "/292704"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.offenceId").value(292704))
            .andExpect(jsonPath("$.cjsCode").value("CW96023"))
            .andExpect(jsonPath("$.offenceTitle").value("Use a chemical weapon"))
            .andExpect(jsonPath("$.offenceTitleCy").doesNotExist());
    }


    @Test
    @DisplayName("Get offence reference data")
    void testGetOffenceReferenceData() throws Exception {
        OffenceEntity offenceEntity = createOffenceEntity();
        ResultActions actions = mockMvc.perform(get(URL_BASE).param("cjs_code","CW96023"));
        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetOffenceReferenceData: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.refData[0].offence_id").value(53115))
            .andExpect(jsonPath("$.refData[0].cjs_code").value("CW96023"))
            .andExpect(jsonPath("$.refData[0].offence_title").value("Use a chemical weapon"));

        assertTrue(jsonSchemaValidationService.isValid(body, GET_OFFENCES_REF_DATA_RESPONSE));
    }

    @Test
    @DisplayName("Get no offences returned when offence does not exist [@PO-420, PO-272]")
    void testGetOffenceById_WhenOffenceDoesNotExist() throws Exception {
        mockMvc.perform(get(URL_BASE + "/999999"))
            .andExpect(status().isNotFound()); ///.andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Post search result for offence created by POST request [@PO-926, PO-304]")
    void testPostOffencesSearch() throws Exception {
        MvcResult result = mockMvc.perform(post(URL_BASE + "/search")
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .content("{\"cjs_code\":\"IC01001\"}"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(4))
            .andExpect(jsonPath("$.searchData[0].offence_id").value(33430))
            .andExpect(jsonPath("$.searchData[0].cjs_code").value("IC01001"))
            .andExpect(jsonPath("$.searchData[0].offence_title").value("Genocide"))
            .andExpect(jsonPath("$.searchData[0].date_used_from").value("2001-09-01T00:00:00"))
            .andExpect(jsonPath("$.searchData[0].offence_oas")
                .value("Contrary to sections 51 and 53 of the International Criminal Court Act 2001."))
            .andReturn();

        String body = result.getResponse().getContentAsString();
        log.info(":testPostOffencesSearch: Response body:\n" + ToJsonString.toPrettyJson(body));
        assertTrue(jsonSchemaValidationService.isValid(body, POST_OFFENCES_SEARCH_RESPONSE));

    }

    @Test
    @DisplayName("Post no search result when offence does not exist [@PO-926, PO-304]")
    void testPostOffencesSearch_WhenOffenceDoesNotExist() throws Exception {
        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"cjs_code\":\"NOTREALCODE\"}"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    void testGetOffencesWithCjsCode() throws Exception {
        mockMvc.perform(get(URL_BASE)
                .param("cjs_code", "CW96023"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.refData[0].cjs_code").value("CW96023"))
            .andExpect(jsonPath("$.refData[0].offence_title")
                .value("Use a chemical weapon"))
            .andExpect(jsonPath("$.refData[0].offence_oas")
                .value("Contrary to section 2(1)(a) and (8) of the Chemical Weapons Act 1996."));
    }


    private OffenceEntity createOffenceEntity() {
        return OffenceEntity.builder()
            .offenceId(1L)
            .cjsCode("cjs-code")
            .businessUnit(BusinessUnitEntity.builder().build())
            .offenceTitle("Title of Offence")
            .offenceTitleCy("Title of Offence CY")
            .build();
    }

    private OffenceReferenceData createOffenceReferenceData() {
        return new OffenceReferenceData(1L, "TH123456", (short)007,
                                        "Thief of Time", null,
                                        LocalDateTime.of(1909, 3, 3, 3, 30),
                                        null, "An Important Offence", "");
    }

    private OffenceSearchData createOffenceSearchData() {
        return new OffenceSearchData(1L, "TH123456",
                                     "Thief of Time", null,
                                     LocalDateTime.of(1909, 3, 3, 3, 30),
                                     null, "An Important Offence", "");
    }
}
