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
import uk.gov.hmcts.opal.dto.search.OffenceSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.OffenceEntity;
import uk.gov.hmcts.opal.entity.projection.OffenceReferenceData;
import uk.gov.hmcts.opal.entity.projection.OffenceSearchData;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.gov.hmcts.opal.service.opal.OffenceService;

import java.time.LocalDateTime;
import java.util.List;

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
@ContextConfiguration(classes = OffenceController.class)
@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.OffenceControllerIntegrationTest")
@DisplayName("OffenceController Integration Test")
class OffenceControllerIntegrationTest {

    private static final String URL_BASE = "/offences";
    private static final String GET_OFFENCES_REF_DATA_RESPONSE = "getOffencesRefDataResponse.json";
    private static final String POST_OFFENCES_SEARCH_RESPONSE = "postOffencesSearchResponse.json";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    @Qualifier("offenceServiceProxy")
    OffenceService offenceService;

    @SpyBean
    private JsonSchemaValidationService jsonSchemaValidationService;

    @Test
    @DisplayName("Get offence by ID [@PO-420, PO-272]")
    void testGetOffenceById() throws Exception {
        OffenceEntity offenceEntity = createOffenceEntity();

        when(offenceService.getOffence((short)1)).thenReturn(offenceEntity);

        mockMvc.perform(get(URL_BASE + "/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.offenceId").value(1))
            .andExpect(jsonPath("$.cjsCode").value("cjs-code"))
            .andExpect(jsonPath("$.offenceTitle").value("Title of Offence"))
            .andExpect(jsonPath("$.offenceTitleCy").value("Title of Offence CY"));
    }


    @Test
    @DisplayName("Get offence reference data")
    void testGetOffenceReferenceData() throws Exception {
        OffenceEntity offenceEntity = createOffenceEntity();

        OffenceReferenceData refData = OffenceService.toRefData(offenceEntity);
        when(offenceService.getReferenceData(any(), any())).thenReturn(List.of(refData));

        ResultActions actions = mockMvc.perform(get(URL_BASE));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetOffenceReferenceData: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.refData[0].offence_id").value(1))
            .andExpect(jsonPath("$.refData[0].cjs_code").value("cjs-code"))
            .andExpect(jsonPath("$.refData[0].offence_title").value("Title of Offence"))
            .andExpect(jsonPath("$.refData[0].offence_title_cy").value("Title of Offence CY"));

        assertTrue(jsonSchemaValidationService.isValid(body, GET_OFFENCES_REF_DATA_RESPONSE));
    }

    @Test
    @DisplayName("Get no offences returned when offence does not exist [@PO-420, PO-272]")
    void testGetOffenceById_WhenOffenceDoesNotExist() throws Exception {
        when(offenceService.getOffence((short)2)).thenReturn(null);

        mockMvc.perform(get(URL_BASE + "/2"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Post search result for offence created by POST request [@PO-926, PO-304]")
    void testPostOffencesSearch() throws Exception {
        OffenceSearchData entity = createOffenceSearchData();

        when(offenceService.searchOffences(any(OffenceSearchDto.class))).thenReturn(singletonList(entity));

        ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .content("{\"criteria\":\"value\"}"));
        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetDraftAccountById: Response body:\n" + ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.searchData[0].offence_id").value(1))
            .andExpect(jsonPath("$.searchData[0].cjs_code").value("TH123456"))
            .andExpect(jsonPath("$.searchData[0].offence_title").value("Thief of Time"))
            .andExpect(jsonPath("$.searchData[0].date_used_from").value("1909-03-03T03:30:00"))
            .andExpect(jsonPath("$.searchData[0].offence_oas").value("An Important Offence"))
            .andReturn();

        assertTrue(jsonSchemaValidationService.isValid(body, POST_OFFENCES_SEARCH_RESPONSE));
    }

    @Test
    @DisplayName("Post no search result when offence does not exist [@PO-926, PO-304]")
    void testPostOffencesSearch_WhenOffenceDoesNotExist() throws Exception {
        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"2\"}"))
            .andExpect(status().isOk());
    }

    private OffenceEntity createOffenceEntity() {
        return OffenceEntity.builder()
            .offenceId(1L)
            .cjsCode("cjs-code")
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short)7).build())
            .offenceTitle("Title of Offence")
            .offenceTitleCy("Title of Offence CY")
            .offenceOas("Offence Act & Section")
            .offenceOasCy("Offence Act & Section CY")
            .dateUsedFrom(LocalDateTime.now().minusDays(1000))
            .dateUsedTo(LocalDateTime.now().plusDays(1000))
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
